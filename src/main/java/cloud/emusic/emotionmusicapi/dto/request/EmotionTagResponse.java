package cloud.emusic.emotionmusicapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "감정 태그 응답 DTO")
public class EmotionTagResponse {

    @Schema(description = "감정 태그 ID", example = "1")
    private Long id;

    @Schema(description = "감정 태그 이름", example = "행복")
    private String name;
}
