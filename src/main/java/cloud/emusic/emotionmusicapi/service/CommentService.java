package cloud.emusic.emotionmusicapi.service;

import cloud.emusic.emotionmusicapi.domain.comment.Comment;
import cloud.emusic.emotionmusicapi.domain.post.Post;
import cloud.emusic.emotionmusicapi.domain.user.User;
import cloud.emusic.emotionmusicapi.dto.request.CommentRequest;
import cloud.emusic.emotionmusicapi.dto.response.comment.CommentResponse;
import cloud.emusic.emotionmusicapi.exception.CustomException;
import cloud.emusic.emotionmusicapi.exception.dto.ErrorCode;
import cloud.emusic.emotionmusicapi.repository.CommentRepository;
import cloud.emusic.emotionmusicapi.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;

  // 댓글 생성
  public CommentResponse createComment(Long postId, CommentRequest request, User user) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

    Comment comment = Comment.builder()
        .content(request.getContent())
        .post(post)
        .user(user)
        .build();

    commentRepository.save(comment);

    return new CommentResponse(comment);
  }

  // 특정 게시글의 모든 댓글 조회
  @Transactional(readOnly = true)
  public List<CommentResponse> getCommentsByPost(Long postId) {
    if (!postRepository.existsById(postId)) {
      throw new CustomException(ErrorCode.POST_NOT_FOUND);
    }
    return commentRepository.findByPostId(postId).stream()
        .map(CommentResponse::new)
        .collect(Collectors.toList());
  }
}