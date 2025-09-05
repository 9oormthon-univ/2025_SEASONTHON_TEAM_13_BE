package cloud.emusic.emotionmusicapi.controller;

import cloud.emusic.emotionmusicapi.dto.response.TagRankingResponseWrapper;
import cloud.emusic.emotionmusicapi.exception.ErrorResponse;
import cloud.emusic.emotionmusicapi.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tags")
@Tag(name = "Tag API", description = "태그 관련 API 문서입니다.")
public class TagController {

    private final TagService tagService;

    @Operation(
            summary = "실시간 태그 검색 랭킹 조회",
            description = "실시간 태그 검색 랭킹을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "태그 랭킹 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TagRankingResponseWrapper.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/ranking")
    public ResponseEntity<TagRankingResponseWrapper> getAllTagRankings() {
        return ResponseEntity.ok(tagService.getTagRankings());
    }

}
