package cloud.emusic.emotionmusicapi.repository;

import cloud.emusic.emotionmusicapi.domain.post.PostLike;
import cloud.emusic.emotionmusicapi.domain.post.Post;
import cloud.emusic.emotionmusicapi.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByUserAndPost(User user, Post post);

    long countByPost(Post post);

    List<PostLike> findAllByUser(User user);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    void deleteByPost(Post post);
}
