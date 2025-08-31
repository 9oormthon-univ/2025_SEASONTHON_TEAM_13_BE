package cloud.emusic.emotionmusicapi.domain;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // Auditing 기능 포함
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @CreatedDate // Entity 생성 시간 자동 저장
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate // Entity 수정 시간 자동 저장
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열로 저장
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "profile_image", nullable = false)
    private String profileImage;

    @Builder
    public User(String email, String nickname, Role role, String profileImage) {
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.profileImage = profileImage;
    }
}
