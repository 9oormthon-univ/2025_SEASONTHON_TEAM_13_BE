package cloud.emusic.emotionmusicapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "게시글 작성 요청 DTO")
public class PostCreateRequest {

    @NotBlank(message = "감정 태그는 최소 1개 이상 선택 해야 합니다.")
    @Size(max = 3, message = "감정 태그는 최대 3개까지 선택할 수 있습니다.")
    private List<String> emotionTags;

    @NotBlank(message = "하루 태그는 최소 1개 이상 작성 해야 합니다.")
    @Size(max = 3, message = "하루 태그는 최대 3개까지 작성 할 수 있습니다.")
    private List<String> dailyTags;

    @NotBlank(message = "노래 트랙 ID는 필수입니다.")
    private String songTrackId;

}
