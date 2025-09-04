package cloud.emusic.emotionmusicapi.repository;

import cloud.emusic.emotionmusicapi.domain.Like;
import cloud.emusic.emotionmusicapi.domain.Post;
import cloud.emusic.emotionmusicapi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

}
