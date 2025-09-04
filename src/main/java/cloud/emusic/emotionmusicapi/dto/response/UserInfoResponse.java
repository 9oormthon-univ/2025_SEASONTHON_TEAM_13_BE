package cloud.emusic.emotionmusicapi.dto.response;

import cloud.emusic.emotionmusicapi.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    @Schema(description = "사용자 ID", example = "1" )
    private Long userId;

    @Schema(description = "사용자 이름", example = "test_user" )
    private String username;

    @Schema(description = "사용자 이메일", example = "example.com")
    private String email;

    @Schema(description = "사용자 프로필 URL", example = "https://example.com/profile.jpg" )
    private String profileUrl;

    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .userId(user.getId())
                .username(user.getNickname())
                .email(user.getEmail())
                .profileUrl(user.getProfileImage())
                .build();
    }
}
