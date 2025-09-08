package cloud.emusic.emotionmusicapi.dto.response.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "태그 랭킹 응답 DTO")
public class TagRankingResponse {

    @Schema(description = "태그 이름", example = "tag1")
    private String tagName;

    @Schema(description = "태그 사용 횟수", example = "150")
    private Long tagCount;

}
