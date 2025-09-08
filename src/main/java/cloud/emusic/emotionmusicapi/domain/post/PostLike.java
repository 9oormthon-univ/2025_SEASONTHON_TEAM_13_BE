package cloud.emusic.emotionmusicapi.domain.post;

import cloud.emusic.emotionmusicapi.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_like",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "post_id"}) // 한 유저가 같은 게시글에 중복 좋아요 방지
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    private PostLike(User user, Post post) {
        this.user = user;
        this.post = post;
    }

    public static PostLike create(User user, Post post) {
        return new PostLike(user, post);
    }
}
