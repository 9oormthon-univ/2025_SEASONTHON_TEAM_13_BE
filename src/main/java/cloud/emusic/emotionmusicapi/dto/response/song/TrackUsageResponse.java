package cloud.emusic.emotionmusicapi.dto.response.song;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "노래 사용 통계 응답 DTO")
public class TrackUsageResponse {

    @Schema(description = "노래 트랙 ID", example = "3n3Ppam7vgaVa1iaRUc9Lp")
    private String trackId;

    @Schema(description = "노래 제목", example = "Mr. Brightside")
    private String trackTitle;

    @Schema(description = "노래 아티스트", example = "The Killers")
    private String artist;

    @Schema(description = "노래 앨범", example = "url")
    private String albumImageUrl;

    @Schema(description = "노래 사용 횟수", example = "42")
    private long usageCount;

    @Schema(description = "노래 재생 횟수", example = "10")
    private long playCount;
}
