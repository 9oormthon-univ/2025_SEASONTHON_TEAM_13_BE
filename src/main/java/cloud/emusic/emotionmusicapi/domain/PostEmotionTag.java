package cloud.emusic.emotionmusicapi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "post_emotion_tag")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class PostEmotionTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_emotion_tag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emotion_tag_id", nullable = false)
    private EmotionTag emotionTag;
}
