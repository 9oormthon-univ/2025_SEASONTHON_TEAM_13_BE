package cloud.emusic.emotionmusicapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "게시글 생성 응답 DTO")
public class PostCreateResponse {
    @Schema(description = "생성된 게시글 ID", example = "1")
    private Long postId;
}
