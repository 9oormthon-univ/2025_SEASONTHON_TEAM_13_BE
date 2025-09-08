package cloud.emusic.emotionmusicapi.repository;

import cloud.emusic.emotionmusicapi.domain.tag.PostEmotionTag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostEmotionTagRepository extends JpaRepository<PostEmotionTag, Long> {

    @Query("""
    SELECT et.name, COUNT(pet.post.id)
    FROM PostEmotionTag pet
    JOIN pet.emotionTag et
    GROUP BY et.name
    ORDER BY COUNT(pet.post.id) DESC
""")
    List<Object[]> findTopEmotionTags(Pageable pageable);
}
