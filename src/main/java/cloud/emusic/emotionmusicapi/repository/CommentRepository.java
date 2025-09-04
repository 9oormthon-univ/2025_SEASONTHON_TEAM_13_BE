package cloud.emusic.emotionmusicapi.repository;

import cloud.emusic.emotionmusicapi.domain.Comment;
import cloud.emusic.emotionmusicapi.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
  // 특정 게시글에 달린 모든 댓글을 조회
  List<Comment> findByPostId(Long postId);

  long countByPost(Post post);

  void deleteByPost(Post post);
}