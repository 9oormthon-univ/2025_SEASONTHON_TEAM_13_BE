package cloud.emusic.emotionmusicapi.controller;

import cloud.emusic.emotionmusicapi.domain.User;
import cloud.emusic.emotionmusicapi.dto.request.CommentRequest;
import cloud.emusic.emotionmusicapi.dto.response.CommentResponse;
import cloud.emusic.emotionmusicapi.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/comments")
@Tag(name = "Comment API", description = "댓글 관련 API")
public class CommentController {

  private final CommentService commentService;

  @PostMapping
  @Operation(summary = "댓글 작성", description = "특정 게시글에 댓글을 작성합니다.")
  public ResponseEntity<CommentResponse> createComment(
      @PathVariable Long postId,
      @Valid @RequestBody CommentRequest request,
      @AuthenticationPrincipal User user) {
    CommentResponse response = commentService.createComment(postId, request, user);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 모든 댓글을 조회합니다.")
  public ResponseEntity<List<CommentResponse>> getCommentsByPost(@PathVariable Long postId) {
    List<CommentResponse> comments = commentService.getCommentsByPost(postId);
    return ResponseEntity.ok(comments);
  }
}