package cloud.emusic.emotionmusicapi.domain.tag;

import cloud.emusic.emotionmusicapi.domain.post.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "day_tag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DayTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "day_tag_id")
    private Long id;

    @Column(nullable = false,length = 500)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static DayTag create(String name,Post post) {
        DayTag tag = new DayTag();
        tag.name = name;
        tag.post = post;
        return tag;
    }
}
