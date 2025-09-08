package cloud.emusic.emotionmusicapi.repository;

import cloud.emusic.emotionmusicapi.domain.song.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SongRepository extends JpaRepository<Song, Long> {
  Optional<Song> findByTrackId(String trackId);
}