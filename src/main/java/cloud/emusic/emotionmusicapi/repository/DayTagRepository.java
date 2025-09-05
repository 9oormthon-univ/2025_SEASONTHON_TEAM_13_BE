package cloud.emusic.emotionmusicapi.repository;

import cloud.emusic.emotionmusicapi.domain.DayTag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DayTagRepository extends JpaRepository<DayTag, Long> {

    @Query("""
    SELECT dt.name, COUNT(dt.post.id)
    FROM DayTag dt
    GROUP BY dt.name
    ORDER BY COUNT(dt.post.id) DESC
""")
    List<Object[]> findTopDayTags(Pageable pageable);
}
