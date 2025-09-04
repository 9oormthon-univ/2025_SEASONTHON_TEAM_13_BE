package cloud.emusic.emotionmusicapi.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "post_emotion_tag",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "emotion_tag_id"}) // 중복 방지
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostEmotionTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_emotion_tag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emotion_tag_id", nullable = false)
    private EmotionTag emotionTag;

    public PostEmotionTag(Post post, EmotionTag emotionTag) {
        this.post = post;
        this.emotionTag = emotionTag;
    }
}
