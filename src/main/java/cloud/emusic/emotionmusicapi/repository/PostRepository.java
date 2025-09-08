package cloud.emusic.emotionmusicapi.repository;

import cloud.emusic.emotionmusicapi.domain.post.Post;
import cloud.emusic.emotionmusicapi.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByUser(User user);

    @Query("SELECT p FROM Post p WHERE p.user = :user AND p.createdAt >= :start AND p.createdAt < :end")
    Optional<Post> findByUserAndCreatedDate(
            @Param("user") User user,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

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

    @Query("""
SELECT DISTINCT p 
FROM Post p
LEFT JOIN p.emotionTags et
LEFT JOIN p.dayTags dt
WHERE (:tag IS NULL OR et.emotionTag.name = :tag OR dt.name = :tag)
""")
    Page<Post> searchPostsByTag(
            @Param("tag") String tag,
            Pageable pageable
    );

}
