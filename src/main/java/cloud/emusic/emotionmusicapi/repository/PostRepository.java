package cloud.emusic.emotionmusicapi.repository;

import cloud.emusic.emotionmusicapi.domain.Post;
import cloud.emusic.emotionmusicapi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByUser(User user);

    @Query(value = """
    SELECT p.*, COUNT(pl.post_id) AS like_count
    FROM post p
    LEFT JOIN post_like pl ON p.post_id = pl.post_id
    GROUP BY p.post_id
    ORDER BY like_count DESC
    LIMIT :limit OFFSET :offset
""", nativeQuery = true)
    List<Object[]> findPostsOrderByLikeCount(@Param("limit") int limit, @Param("offset") int offset);

    @Query("SELECT DISTINCT p FROM Post p " +
            "JOIN FETCH p.song " +
            "JOIN FETCH p.emotionTags et " +
            "JOIN FETCH et.emotionTag")
    List<Post> findAllWithTrackAndTags();
}
