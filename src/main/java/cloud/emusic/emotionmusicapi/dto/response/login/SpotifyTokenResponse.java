package cloud.emusic.emotionmusicapi.dto.response.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Spotify 토큰 응답 DTO")
public class SpotifyTokenResponse {

    @Schema(description = "액세스 토큰", example = "BQD...")
    @JsonProperty("access_token")
    private String accessToken;

    @Schema(description = "토큰 유효시간 (초단위)", example = "3600")
    @JsonProperty("expires_in")
    private int expiresIn;
}
