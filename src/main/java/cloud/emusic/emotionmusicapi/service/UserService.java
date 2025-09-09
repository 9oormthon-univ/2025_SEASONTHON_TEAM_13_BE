package cloud.emusic.emotionmusicapi.service;


import cloud.emusic.emotionmusicapi.domain.user.Role;
import cloud.emusic.emotionmusicapi.domain.user.User;
import cloud.emusic.emotionmusicapi.domain.user.UserStatus;
import cloud.emusic.emotionmusicapi.dto.response.login.KakaoUserResponse;
import cloud.emusic.emotionmusicapi.dto.response.user.UserInfoResponse;
import cloud.emusic.emotionmusicapi.exception.CustomException;
import cloud.emusic.emotionmusicapi.exception.dto.ErrorCode;
import cloud.emusic.emotionmusicapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User processKakaoUser(KakaoUserResponse kakaoUserResponse) {

        String email = kakaoUserResponse.getKakaoAccount().getEmail();
        String nickname = kakaoUserResponse.getKakaoAccount().getProfile().getNickname();
        String profileImage = kakaoUserResponse.getKakaoAccount().getProfile().getProfileImageUrl();

        // Find user by email, or create a new one if not exists
        return userRepository.findByEmail(kakaoUserResponse.getKakaoAccount().getEmail())
            .map(user -> {
                if (user.getStatus() == UserStatus.BLOCKED) {
                    throw new CustomException(ErrorCode.USER_BLOCKED);
                }
                return user.updateProfile(nickname, profileImage);
            })
            .orElseGet(() -> { // If user does not exist, create and save a new user
                User newUser = User.builder()
                    .email(email)
                    .nickname(nickname)
                    .profileImage(profileImage)
                    .role(Role.USER) // Default role
                    .build();
                return userRepository.save(newUser);
            });
    }


    @Transactional
    public void signUp(){
        //User객체 생성
        User newUser = User.builder()
            .email("testing@email.com")
            .nickname("Tester")
            .role(Role.USER)
            .profileImage("default_img_url")
            .build();

        //JPA를 통한 Save
        userRepository.save(newUser);
    }

    public UserInfoResponse getUserInfo(Long userId){
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return UserInfoResponse.from(user);
    }
}