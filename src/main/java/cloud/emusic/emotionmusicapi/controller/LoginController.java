package cloud.emusic.emotionmusicapi.controller;
import cloud.emusic.emotionmusicapi.dto.request.LoginRequest;
import cloud.emusic.emotionmusicapi.dto.response.KakaoTokenResponse;
import cloud.emusic.emotionmusicapi.dto.response.KakaoUserResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/login")
public class LoginController {

  // 1. final 키워드를 사용해 불변 필드(멤버 변수)로 선언합니다.
  private final String kakaoRestApiKey;
  private final String kakaoRedirectUri;
  private final String kakaoClientSecret;


  // 2. 생성자를 만들어 @Value 어노테이션으로 값을 주입받습니다. (✅ 권장 방식)
  // Spring이 LoginController Bean을 생성할 때 이 생성자를 호출하여 값을 넣어줍니다.
  public LoginController(
      @Value("${KAKAO_REST_API_KEY}") String kakaoRestApiKey,
      @Value("${KAKAO_REDIRECT_URI}") String kakaoRedirectUri,
      @Value("${KAKAO_CLIENT_SECRET}") String kakaoClientSecret) {
    this.kakaoRestApiKey = kakaoRestApiKey;
    this.kakaoRedirectUri = kakaoRedirectUri;
    this.kakaoClientSecret = kakaoClientSecret;
  }

  @GetMapping("/url")
  public String getLoginUrl() {
    // 실제 로그인 URL을 반환하는 로직
    return "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" +
        this.kakaoRestApiKey +
        "&redirect_uri=" +
        this.kakaoRedirectUri+
        "&scope=account_email,profile_nickname,profile_image";
  }

  @GetMapping("/authenticate")
  public KakaoUserResponse authenticate(@ParameterObject LoginRequest request) {

    WebClient webClient = WebClient.builder()
        .baseUrl("https://kauth.kakao.com")
        .build();

    // 1. 액세스 토큰 요청
    KakaoTokenResponse tokenResponse =
        webClient.post()
            .uri("/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("grant_type=authorization_code"
                + "&client_id=" + kakaoRestApiKey
                + "&client_secret=" + kakaoClientSecret
                + "&redirect_uri=" + kakaoRedirectUri
                + "&code=" + request.getCode())
            .retrieve()
            .bodyToMono(KakaoTokenResponse.class)
            .block();  // 동기적으로 결과 받기

    // 2. 유저 정보 요청
    WebClient apiClient = WebClient.builder()
        .baseUrl("https://kapi.kakao.com")
        .build();

    KakaoUserResponse userResponse =
        apiClient.get()
            .uri("/v2/user/me")
            .header("Authorization", "Bearer " + tokenResponse.getAccessToken())
            .retrieve()
            .bodyToMono(KakaoUserResponse.class)
            .block();  // 동기적으로 결과 받기

    return userResponse;  // JSON 형태로 임시반환.

    //추가적인 JWT 발행 요망
  }


}

