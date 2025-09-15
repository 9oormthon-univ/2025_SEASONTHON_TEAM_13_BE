package cloud.emusic.emotionmusicapi.repository;

import cloud.emusic.emotionmusicapi.domain.post.PostLike;
import cloud.emusic.emotionmusicapi.domain.post.Post;
import cloud.emusic.emotionmusicapi.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByUserAndPost(User user, Post post);

    long countByPost(Post post);

    List<PostLike> findAllByUser(User user);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    void deleteByPost(Post post);

    @Query("""
        SELECT COUNT(l)
        FROM PostLike l
        JOIN l.post p
        WHERE p.user.id = :userId
    """)
    int countAllLikesByUserId(@Param("userId") Long userId);
}
