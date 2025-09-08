package cloud.emusic.emotionmusicapi.repository;

import cloud.emusic.emotionmusicapi.domain.tag.EmotionTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmotionTagRepository extends JpaRepository<EmotionTag, Long> {
    Optional<EmotionTag> findByName(String name);
}
