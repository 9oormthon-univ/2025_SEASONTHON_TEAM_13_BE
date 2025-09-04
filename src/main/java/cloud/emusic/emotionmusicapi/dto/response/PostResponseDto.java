package cloud.emusic.emotionmusicapi.dto.response;

import cloud.emusic.emotionmusicapi.domain.DayTag;
import cloud.emusic.emotionmusicapi.domain.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {
    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    @Schema(description = "감정 태그", example = "[\"행복\", \"슬픔\"]")
    private List<String> emotionTags;

    @Schema(description = "하루 태그", example = "[\"운동\", \"공부\"]")
    private List<String> dailyTags;

    @Schema(description = "노래 트랙 ID", example = "12345")
    private String trackId;

    @Schema(description = "작성자", example = "user123")
    private String user;

    @Schema(description = "좋아요 수", example = "10")
    private Long likeCount;

    @Schema(description = "작성일", example = "2025-09-04T12:34:56")
    private LocalDateTime createdAt;

    @Schema(description = "수정일", example = "2025-09-05T14:56:23")
    private LocalDateTime updatedAt;

    public static PostResponseDto from(Post post,Long likeCount) {
        return PostResponseDto.builder()
                .id(post.getId())
                .emotionTags(
                        post.getEmotionTags().stream()
                                .map(et -> et.getEmotionTag().getName())
                                .toList()
                )
                .dailyTags(
                        post.getDayTags().stream()
                                .map(DayTag::getName)
                                .toList()
                )
                .trackId(post.getSongTrackId())
                .user(post.getUser().getNickname())
                .likeCount(likeCount)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
