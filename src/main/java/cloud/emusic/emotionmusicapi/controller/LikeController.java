package cloud.emusic.emotionmusicapi.controller;

import cloud.emusic.emotionmusicapi.dto.response.post.PostResponse;
import cloud.emusic.emotionmusicapi.exception.ApiExceptions;
import cloud.emusic.emotionmusicapi.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cloud.emusic.emotionmusicapi.exception.dto.ErrorCode.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
@Tag(name = "Like API", description = "좋아요 관련 API 문서")
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "게시글 좋아요", description = "특정 게시글에 좋아요를 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 성공")
    })
    @ApiExceptions(values = {POST_NOT_FOUND,ALREADY_LIKED,UNAUTHORIZED,INTERNAL_SERVER_ERROR})
    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> likePost(@PathVariable Long postId,@AuthenticationPrincipal(expression = "id") Long userId) {
        likeService.likePost(postId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "게시글 좋아요 취소", description = "특정 게시글의 좋아요를 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 취소 성공")
    })
    @ApiExceptions(values = {POST_NOT_FOUND,LIKE_NOT_FOUND,UNAUTHORIZED,INTERNAL_SERVER_ERROR})
    @DeleteMapping("/{postId}/like")
    public ResponseEntity<Void> unlikePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        likeService.unLikePost(postId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내가 좋아요한 게시글 목록 조회", description = "로그인한 사용자가 좋아요한 게시글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요한 게시글 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PostResponse.class)))),
    })
    @ApiExceptions(values = {UNAUTHORIZED,INTERNAL_SERVER_ERROR})
    @GetMapping("/me/likes")
    public ResponseEntity<List<PostResponse>> getMyLikedPosts(
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        return ResponseEntity.ok(likeService.getLikedPosts(userId));
    }

}
