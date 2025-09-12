package cloud.emusic.emotionmusicapi.controller;

import cloud.emusic.emotionmusicapi.dto.response.song.TrackResponse;
import cloud.emusic.emotionmusicapi.service.SpotifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SpotifyController {

    private final SpotifyService spotifyService;

    @Operation(
            summary = "감정 기반 노래 추천",
            description = "사용자가 선택한 감정 태그(최대 3개)를 기반으로 최신 K-pop 곡을 추천합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "추천 곡 목록 반환 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = TrackResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/recommend")
    public ResponseEntity<List<TrackResponse>> recommend(
            @Parameter(
                    description = "감정 태그 목록 (최대 3개). 예: emotions=슬픔,외로움,피곤함",
                    example = "슬픔,외로움,피곤함"
            )
            @RequestParam List<String> emotions,

            @Parameter(
                    description = "추천 곡 개수 (기본값 10)",
                    example = "10"
            )
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(spotifyService.recommendByEmotions(emotions, limit));
    }

    @Operation(
            summary = "곡명으로 노래 검색",
            description = "Spotify API를 이용해 곡명으로 노래를 검색합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 결과 반환 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TrackResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/search")
    public TrackResponse searchByName(
            @Parameter(description = "검색할 곡명", example = "Blueming")
            @RequestParam String query
    ) {
        return spotifyService.searchTracksByTitle(query);
    }

    @Operation(
            summary = "게시글 노래 재생 횟수 증가",
            description = "해당 게시글의 노래의 재생 횟수를 증가 합니다"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "게시글 노래 재생 횟수 증가 성공"
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/{trackId}/count")
    public ResponseEntity<Void> songCountUp(@PathVariable String trackId){
        spotifyService.songCountUp(trackId);
        return ResponseEntity.noContent().build();
    }
}