package cloud.emusic.emotionmusicapi.controller;

import cloud.emusic.emotionmusicapi.dto.response.tag.EmotionTagResponse;
import cloud.emusic.emotionmusicapi.dto.request.PostCreateRequest;
import cloud.emusic.emotionmusicapi.dto.response.post.PostCreateResponse;
import cloud.emusic.emotionmusicapi.dto.response.post.PostResponse;
import cloud.emusic.emotionmusicapi.exception.ApiExceptions;
import cloud.emusic.emotionmusicapi.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static cloud.emusic.emotionmusicapi.exception.dto.ErrorCode.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
@Tag(name = "Post API", description = "게시글 관련 API 문서")
public class PostController {

    private final PostService postService;

    @Operation(
            summary = "감정 태그 목록 조회",
            description = "게시글 작성 시 선택할 수 있는 감정 태그 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "감정 태그 목록 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = EmotionTagResponse.class))
                    )
            )
    })
    @ApiExceptions(values = {EMOTION_TAGS_NOT_FOUND,INTERNAL_SERVER_ERROR})
    @GetMapping("/emotion-tags")
    public ResponseEntity<List<EmotionTagResponse>> getAllEmotionTag() {
        return ResponseEntity.ok(postService.EmotionTag());
    }

    @Operation(summary = "게시글 작성", description = "JWT 인증된 사용자가 게시글을 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시글 작성 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PostCreateResponse.class)
                    )
            )
    })
    @ApiExceptions(values = {UNAUTHORIZED,INTERNAL_SERVER_ERROR})
    @PostMapping
    public ResponseEntity<PostCreateResponse> create(
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal(expression = "id") Long userId) {
        return ResponseEntity.status(201).body(postService.createPost(userId, request));
    }

    @Operation(summary = "게시글 목록 조회", description = "모든 게시글을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PostResponse.class))))
    })
    @ApiExceptions(values = {UNAUTHORIZED,POST_NOT_FOUND,INTERNAL_SERVER_ERROR})
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Parameter(description = "정렬 기준 (예: createdAt, likeCount)")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "정렬 방식 (asc 또는 desc)")
            @RequestParam(defaultValue = "desc") String direction,

            @Parameter(description = "조회할 페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "조회할 페이지 크기")
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getAllPosts(userId,sortBy,direction,page,size));
    }

    @Operation(summary = "게시글 단건 조회", description = "게시글 ID를 사용하여 특정 게시글을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PostResponse.class)))
    })
    @ApiExceptions(values = {POST_NOT_FOUND,UNAUTHORIZED,INTERNAL_SERVER_ERROR})
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostById(
            @PathVariable Long postId,
            @AuthenticationPrincipal(expression = "id") Long userId){
        return ResponseEntity.ok(postService.getPost(postId,userId));
    }

    @Operation(summary = "게시글 수정", description = "게시글 ID를 사용하여 특정 게시글을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PostResponse.class))),
    })
    @ApiExceptions(values = {POST_NOT_FOUND,INVALID_PERMISSION,UNAUTHORIZED,INTERNAL_SERVER_ERROR})
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @RequestBody PostCreateRequest requestDto,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        return ResponseEntity.ok(postService.updatePost(postId, requestDto, userId));
    }

    @Operation(summary = "게시글 삭제", description = "게시글 ID를 사용하여 특정 게시글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "게시글 삭제 성공"),
    })
    @ApiExceptions(values = {POST_NOT_FOUND,UNAUTHORIZED,INTERNAL_SERVER_ERROR})
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        postService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "감정 캘린더 조회", description = "로그인한 사용자가 작성한 게시글을 날짜별로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PostResponse.class))),
    })
    @ApiExceptions(values = {UNAUTHORIZED,INTERNAL_SERVER_ERROR})
    @GetMapping("/calendar")
    public ResponseEntity<List<PostResponse>> getMyEmotionCalendar(
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        return ResponseEntity.ok(postService.getPostCalendar(userId));
    }

    @Operation(summary = "자신이 작성한 게시글 날자별 단건 조회", description = "로그인한 사용자가 작성한 게시글을 날짜별로 단건 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PostResponse.class))),
    })
    @ApiExceptions(values = {UNAUTHORIZED,INTERNAL_SERVER_ERROR})
    @GetMapping("/me")
    public ResponseEntity<Object> getMyPost(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate createdDate
    ) {
        PostResponse response = postService.getMyPost(userId, createdDate);
        return ResponseEntity.ok(Objects.requireNonNullElse(response, "null"));
    }

    @Operation(summary = "게시글 검색", description = "하루 태그, 감정 태그로 게시글을 검색합니다. (감정,하루 태그 선택 가능)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PostResponse.class)))),
    })
    @ApiExceptions(values = {INTERNAL_SERVER_ERROR})
    @GetMapping("/search")
    public ResponseEntity<List<PostResponse>> searchPosts(
            @AuthenticationPrincipal(expression = "id") Long userId,

            @Parameter(description = "검색할 감정,하루 태그 (optional)")
            @RequestParam(required = false) String tag,

            @Parameter(description = "조회할 페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "조회할 페이지 크기")
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                postService.searchPostsTag(userId, tag, page, size)
        );
    }

}
