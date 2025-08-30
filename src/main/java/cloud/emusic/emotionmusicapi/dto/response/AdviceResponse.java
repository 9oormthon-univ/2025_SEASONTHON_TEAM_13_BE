package cloud.emusic.emotionmusicapi.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdviceResponse {

    @Schema(description = "명언 작성자", example = "에이브러햄 링컨")
    private String author;

    @Schema(description = "작성자 프로필/소개", example = "미국 16대 대통령")
    @JsonProperty("authorProfile") // JSON의 'authorProfile' 필드를 'authorProfile' 변수에 매핑
    private String authorProfile;

    @Schema(description = "명언 내용", example = "반드시 이겨야 하는 건 아니지만 진실할 필요는 있다...")
    private String message;
}
