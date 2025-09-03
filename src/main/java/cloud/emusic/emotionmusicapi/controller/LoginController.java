package cloud.emusic.emotionmusicapi.controller;
import cloud.emusic.emotionmusicapi.config.JwtTokenProvider;
import cloud.emusic.emotionmusicapi.domain.User;
import cloud.emusic.emotionmusicapi.dto.request.LoginRequest;
import cloud.emusic.emotionmusicapi.dto.response.KakaoTokenResponse;
import cloud.emusic.emotionmusicapi.dto.response.KakaoUserResponse;
import cloud.emusic.emotionmusicapi.dto.response.LoginResponse;
import cloud.emusic.emotionmusicapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

  private final UserService userService;
  private final JwtTokenProvider jwtTokenProvider;

  @Value("${KAKAO_REST_API_KEY}")
  private String kakaoRestApiKey;

  @Value("${KAKAO_REDIRECT_URI}")
  private String kakaoRedirectUri;

  @Value("${KAKAO_CLIENT_SECRET}")
  private String kakaoClientSecret;


  @GetMapping("/url")
  public String getLoginUrl() {
    return "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" +
        this.kakaoRestApiKey +
        "&redirect_uri=" +
        this.kakaoRedirectUri+
        "&scope=account_email,profile_nickname,profile_image";
  }

  @GetMapping("/authenticate")
  public LoginResponse authenticate(@ParameterObject LoginRequest request) {

    // 1. Get Access Token from Kakao
    KakaoTokenResponse tokenResponse = getKakaoToken(request.getCode());

    // 2. Get User Info from Kakao
    KakaoUserResponse userResponse = getKakaoUser(tokenResponse.getAccessToken());

    // 3. Process User: Save or Update user in DB
    User user = userService.processKakaoUser(userResponse);

    // 4. Generate JWT Token
    String accessToken = jwtTokenProvider.generateToken(user);

    // 5. Return JWT Token in Response
    return LoginResponse.builder()
        .grantType("Bearer")
        .accessToken(accessToken)
        .build();
  }

  private KakaoTokenResponse getKakaoToken(String code) {
    WebClient webClient = WebClient.builder()
        .baseUrl("https://kauth.kakao.com")
        .build();

    return webClient.post()
        .uri("/oauth/token")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .bodyValue("grant_type=authorization_code"
            + "&client_id=" + kakaoRestApiKey
            + "&client_secret=" + kakaoClientSecret
            + "&redirect_uri=" + kakaoRedirectUri
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