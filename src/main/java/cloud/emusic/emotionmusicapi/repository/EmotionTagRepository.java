package cloud.emusic.emotionmusicapi.repository;

import cloud.emusic.emotionmusicapi.domain.EmotionTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmotionTagRepository extends JpaRepository<EmotionTag, Long> {
}
