package cloud.emusic.emotionmusicapi.repository;

import cloud.emusic.emotionmusicapi.domain.Post;
import cloud.emusic.emotionmusicapi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByUser(User user);
}
