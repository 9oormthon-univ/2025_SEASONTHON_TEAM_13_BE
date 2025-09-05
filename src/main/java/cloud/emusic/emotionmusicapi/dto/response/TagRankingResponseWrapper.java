package cloud.emusic.emotionmusicapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "태그 랭킹 응답 전체 구조")
public class TagRankingResponseWrapper {

    @Schema(description = "게시글 태그 랭킹 리스트")
    private List<TagRankingResponse> emotionTags;

    @Schema(description = "하루 태그 랭킹 리스트")
    private List<TagRankingResponse> dayTags;
}
