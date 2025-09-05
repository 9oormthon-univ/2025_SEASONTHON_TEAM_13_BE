package cloud.emusic.emotionmusicapi.dto.response;

import cloud.emusic.emotionmusicapi.domain.DayTag;
import cloud.emusic.emotionmusicapi.domain.Post;
import cloud.emusic.emotionmusicapi.domain.Song;
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

    @Schema(description = "노래 정보")
    private SongDto song;

    @Schema(description = "작성자", example = "user123")
    private String user;

    @Schema(description = "작성자 이미지 URL", example = "http://example.com/user123.jpg")
    private String userImageUrl;

    @Schema(description = "좋아요 수", example = "10")
    private Long likeCount;

    @Schema(description = "좋아요 여부", example = "true")
    private Boolean likeState;

    @Schema(description = "댓글 수", example = "5")
    private Long commentCount;

    @Schema(description = "작성일", example = "2025-09-04T12:34:56")
    private LocalDateTime createdAt;

    @Schema(description = "수정일", example = "2025-09-05T14:56:23")
    private LocalDateTime updatedAt;

    public static PostResponseDto from(Post post, Long likeCount, boolean likeState, Long commentCount) {
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
            .song(SongDto.from(post.getSong()))
            .user(post.getUser().getNickname())
            .userImageUrl(post.getUser().getProfileImage())
            .likeCount(likeCount)
            .likeState(likeState)
            .commentCount(commentCount)
            .createdAt(post.getCreatedAt())
            .updatedAt(post.getUpdatedAt())
            .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SongDto {
        @Schema(description = "Spotify 트랙 ID")
        private String trackId;

        @Schema(description = "곡 제목")
        private String title;

        @Schema(description = "아티스트 정보")
        private String artist;

        @Schema(description = "앨범 이미지 URL")
        private String albumArtUrl;

        @Schema(description = "재생 횟수")
        private Integer playCount;

        @Schema(description = "스포티파이 재생 링크")
        private String spotifyPlayUrl;

        public static SongDto from(Song song) {
            return SongDto.builder()
                .trackId(song.getTrackId())
                .title(song.getTitle())
                .artist(song.getArtist())
                .albumArtUrl(song.getAlbumArtUrl())
                .playCount(song.getPlayCount())
                .spotifyPlayUrl(song.getSpotifyPlayUrl())
                .build();
        }
    }
}