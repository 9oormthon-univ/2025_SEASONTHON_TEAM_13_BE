package cloud.emusic.emotionmusicapi.controller;

import cloud.emusic.emotionmusicapi.config.JwtTokenProvider;
import cloud.emusic.emotionmusicapi.domain.User;
import cloud.emusic.emotionmusicapi.dto.request.LoginRequest;
import cloud.emusic.emotionmusicapi.dto.response.KakaoTokenResponse;
import cloud.emusic.emotionmusicapi.dto.response.KakaoUserResponse;
import cloud.emusic.emotionmusicapi.dto.response.LoginResponse;
import cloud.emusic.emotionmusicapi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

  private final UserService userService;
  private final JwtTokenProvider jwtTokenProvider;

  @Value("${KAKAO_REST_API_KEY}")
  private String kakaoRestApiKey;

  @Value("${KAKAO_CLIENT_SECRET}")
  private String kakaoClientSecret;

  @GetMapping("/url")
  public String getLoginUrl(HttpServletRequest request) {
    // 현재 요청 URL을 기반으로 Redirect URI를 동적으로 생성합니다.
    // 예: http://localhost:8080/login/url -> http://localhost:8080/login/authenticate
    String referer = request.getHeader("Referer");
    if (referer == null) {
      return null; // referer가 없을 경우 처리
    }

    // 2. Referer를 파싱해서 스킴 + 호스트 + 포트만 추출
    String baseUri = UriComponentsBuilder.fromHttpUrl(referer)
        .replacePath(null)    // 기존 path 제거
        .replaceQuery(null)   // 쿼리스트링 제거
        .build()
        .toUriString();

    // 3. 원하는 하위 패스 추가
    String newUrl = UriComponentsBuilder.fromHttpUrl(baseUri)
        .path("/oauth/callback")
        .build()
        .toUriString();

    return "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" +
        this.kakaoRestApiKey +
        "&redirect_uri=" +
        newUrl +
        "&scope=account_email,profile_nickname,profile_image";
  }

  @GetMapping("/authenticate")
  public LoginResponse authenticate(@ParameterObject LoginRequest loginRequest, HttpServletRequest request) {

    String referer = request.getHeader("Referer");
    if (referer == null) {
      return null; // referer가 없을 경우 처리
    }

    // 2. Referer를 파싱해서 스킴 + 호스트 + 포트만 추출
    String baseUri = UriComponentsBuilder.fromHttpUrl(referer)
        .replacePath(null)    // 기존 path 제거
        .replaceQuery(null)   // 쿼리스트링 제거
        .build()
        .toUriString();

    // 3. 원하는 하위 패스 추가
    String newUrl = UriComponentsBuilder.fromHttpUrl(baseUri)
        .path("/oauth/callback")
        .build()
        .toUriString();



    // 1. Get Access Token from Kakao
    KakaoTokenResponse tokenResponse = getKakaoToken(loginRequest.getCode(), newUrl);

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