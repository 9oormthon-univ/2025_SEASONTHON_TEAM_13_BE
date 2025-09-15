package cloud.emusic.emotionmusicapi.service;


import cloud.emusic.emotionmusicapi.domain.user.Role;
import cloud.emusic.emotionmusicapi.domain.user.User;
import cloud.emusic.emotionmusicapi.domain.user.UserStatus;
import cloud.emusic.emotionmusicapi.dto.response.login.KakaoUserResponse;
import cloud.emusic.emotionmusicapi.dto.response.login.SpotifyTokenResponse;
import cloud.emusic.emotionmusicapi.dto.response.user.UserInfoResponse;
import cloud.emusic.emotionmusicapi.dto.response.user.UserStateResponse;
import cloud.emusic.emotionmusicapi.exception.CustomException;
import cloud.emusic.emotionmusicapi.exception.dto.ErrorCode;
import cloud.emusic.emotionmusicapi.repository.PostRepository;
import cloud.emusic.emotionmusicapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${SPOTIFY_CLIENT_ID}")
    private String spotifyClientId;

    @Value("${SPOTIFY_CLIENT_SECRET}")
    private String spotifyClientSecret;

    private static final String SPOTIFY_ACCESS_TOKEN_URL = "https://accounts.spotify.com/api/token";

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final RestTemplate restTemplate = new RestTemplate();

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

    public SpotifyTokenResponse exchangeCodeForToken(String code,String redirect_url){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(spotifyClientId, spotifyClientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirect_url);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        return restTemplate.postForObject(SPOTIFY_ACCESS_TOKEN_URL, request, SpotifyTokenResponse.class);
    }

    public UserInfoResponse getUserInfo(Long userId){
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return UserInfoResponse.from(user);
    }

    public UserStateResponse getUserStatus(Long userId){

        int postCount = postRepository.countByUserId(userId);

        String mostUsedEmotion = postRepository.findTopEmotionTagsByUserId(userId)
                .stream()
                .findFirst()
                .orElse("None");

        return UserStateResponse.from(postCount,mostUsedEmotion);
    }
}