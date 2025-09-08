package cloud.emusic.emotionmusicapi.dto.response.tag;

import cloud.emusic.emotionmusicapi.dto.response.song.TrackUsageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "감정 태그별 노래 사용 통계 응답 DTO")
public class EmotionTrackGroupResponse {

    @Schema(description = "감정 태그", example = "happy")
    private String emotionTag;

    @Schema(description = "해당 감정 태그로 사용된 노래의 총 개수", example = "150")
    private long totalSongCount;

    @Schema(description = "해당 감정 태그로 사용된 노래 목록")
    private List<TrackUsageResponse> tracks;

}
