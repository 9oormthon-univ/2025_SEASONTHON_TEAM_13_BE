package cloud.emusic.emotionmusicapi.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KakaoUserResponse {
  private Long id;

  @JsonProperty("connected_at")
  private String connectedAt;

  @JsonProperty("kakao_account")
  private KakaoAccount kakaoAccount;

  @Data
  public static class KakaoAccount {
    private String email;
    private Profile profile;

    @Data
    public static class Profile {
      private String nickname;
      @JsonProperty("profile_image_url")
      private String profileImageUrl;
    }
  }
}
