package cloud.emusic.emotionmusicapi.controller;

import cloud.emusic.emotionmusicapi.config.jwt.JwtTokenProvider;
import cloud.emusic.emotionmusicapi.domain.user.User;
import cloud.emusic.emotionmusicapi.dto.request.LoginRequest;
import cloud.emusic.emotionmusicapi.dto.response.login.KakaoTokenResponse;
import cloud.emusic.emotionmusicapi.dto.response.login.KakaoUserResponse;
import cloud.emusic.emotionmusicapi.dto.response.login.LoginResponse;
import cloud.emusic.emotionmusicapi.dto.response.login.SpotifyTokenResponse;
import cloud.emusic.emotionmusicapi.exception.CustomException;
import cloud.emusic.emotionmusicapi.exception.dto.ErrorCode;
import cloud.emusic.emotionmusicapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;


@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
@Tag(name = "Login API", description = "소셜 로그인 관련 API")
public class LoginController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${KAKAO_REST_API_KEY}")
    private String kakaoRestApiKey;

    @Value("${KAKAO_CLIENT_SECRET}")
    private String kakaoClientSecret;

    @Value("${SPOTIFY_CLIENT_ID}")
    private String spotifyClientId;

    @Operation(summary = "카카오 로그인 URL 생성", description = "카카오 로그인 URL을 생성하여 반환합니다.")
    @GetMapping("/url")
    public String getLoginUrl(HttpServletRequest request) {

        String redirect_url = buildCallbackUrl(request, "/oauth/callback");

        return "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" +
            this.kakaoRestApiKey +
            "&redirect_uri=" +
                redirect_url +
            "&scope=account_email,profile_nickname,profile_image";
    }

    @Operation(summary = "카카오 로그인 인증", description = "카카오 로그인 인증을 처리하고 JWT 토큰을 반환합니다.")
    @GetMapping("/authenticate")
    public LoginResponse authenticate(@ParameterObject LoginRequest loginRequest, HttpServletRequest request) {

        String redirect_url = buildCallbackUrl(request, "/oauth/callback");

        // 1. Get Access Token from Kakao
        KakaoTokenResponse tokenResponse = getKakaoToken(loginRequest.getCode(), redirect_url);

        // 2. Get User Info from Kakao
        KakaoUserResponse userResponse = getKakaoUser(tokenResponse.getAccessToken());

        // 3. Process User: Save or Update user in DB
        User user = userService.processKakaoUser(userResponse);

        // 4. Generate JWT Token
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());

        // 5. Return JWT Token in Response
        return LoginResponse.builder()
            .grantType("Bearer")
            .accessToken(accessToken)
            .build();
    }

    @Operation(summary = "스포티파이 로그인 URL 생성", description = "스포티파이 로그인 URL을 생성하여 반환합니다.")
    @GetMapping("spotify/url")
    public ResponseEntity<String> spotifyLogin(HttpServletRequest request) {

        String scope = "streaming user-read-email user-read-private";
        String state = UUID.randomUUID().toString(); // CSRF 공격 방지를 위한 상태 토큰 생성

        request.getSession().setAttribute("OAUTH_STATE", state);

        String referer = request.getHeader("Referer");
        if (referer == null) {
          return null; // referer가 없을 경우 처리
        }

        String redirect_url = buildCallbackUrl(request, "/auth/callback");

        String url = UriComponentsBuilder.fromHttpUrl("https://accounts.spotify.com/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", spotifyClientId)
                .queryParam("scope", scope)
                .queryParam("redirect_uri", redirect_url)
                .queryParam("state", state)
                .build()
                .toUriString();

        return ResponseEntity.ok(url);
    }

    @Operation(summary = "스포티파이 로그인 인증", description = "스포티파이 로그인 인증을 처리하고 액세스 토큰을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "액세스 토큰 발급 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SpotifyTokenResponse.class)
                    )
            ),
    })
    @GetMapping("spotify/authenticate")
    public ResponseEntity<SpotifyTokenResponse> callback(HttpServletRequest request,
                                                         @Parameter(description = "스포티파이에서 발급한 인증 코드", required = true)
                                                         @RequestParam String code,
                                                         @Parameter(description = "CSRF 방지를 위한 상태 토큰", required = true)
                                                         @RequestParam String state) {

        // 세션에서 원래 state 꺼내기
        String originalState = (String) request.getSession().getAttribute("OAUTH_STATE");

        if (originalState == null || !originalState.equals(state)) {
            throw new CustomException(ErrorCode.SECURITY_STATE_CSRF);
        }

        String redirect_url = buildCallbackUrl(request, "/auth/callback");
        SpotifyTokenResponse tokenResponse = userService.exchangeCodeForToken(code,redirect_url);

        return ResponseEntity.ok().body(tokenResponse);
    }

    private String buildCallbackUrl(HttpServletRequest request, String callbackPath) {

        String referer = request.getHeader("Referer");
        if (referer == null) {
          return null;
        }

        String baseUri = UriComponentsBuilder.fromHttpUrl(referer)
                .replacePath(null)   // 기존 path 제거
                .replaceQuery(null)  // 쿼리 제거
                .build()
                .toUriString();

        return UriComponentsBuilder.fromHttpUrl(baseUri)
                .path(callbackPath)
                .build()
                .toUriString();
    }

    private KakaoTokenResponse getKakaoToken(String code, String redirectUri) {
        WebClient webClient = WebClient.builder()
            .baseUrl("https://kauth.kakao.com")
            .build();

        return webClient.post()
            .uri("/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("grant_type=authorization_code"
                + "&client_id=" + kakaoRestApiKey
                + "&client_secret=" + kakaoClientSecret
                + "&redirect_uri=" + redirectUri
                + "&code=" + code)
            .retrieve()
            .bodyToMono(KakaoTokenResponse.class)
            .block();
    }

    private KakaoUserResponse getKakaoUser(String accessToken) {
        WebClient apiClient = WebClient.builder()
            .baseUrl("https://kapi.kakao.com")
            .build();

        return apiClient.get()
            .uri("/v2/user/me")
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .bodyToMono(KakaoUserResponse.class)
            .block();
    }
}