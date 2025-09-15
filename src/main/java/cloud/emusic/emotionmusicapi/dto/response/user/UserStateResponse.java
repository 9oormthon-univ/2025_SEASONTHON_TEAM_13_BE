package cloud.emusic.emotionmusicapi.dto.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStateResponse {

    @Schema(description = "작성한 게시글 개수", example = "1")
    private int postCount;

    @Schema(description = "주요 감정", example = "기쁨")
    private String mostUsedEmotion;

    public static UserStateResponse from(int postCount, String mostUsedEmotion) {
        return UserStateResponse.builder()
                .postCount(postCount)
                .mostUsedEmotion(mostUsedEmotion)
                .build();
    }
}
