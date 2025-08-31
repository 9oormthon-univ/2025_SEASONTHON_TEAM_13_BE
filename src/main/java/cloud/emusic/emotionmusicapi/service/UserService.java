package cloud.emusic.emotionmusicapi.service;


import cloud.emusic.emotionmusicapi.domain.Role;
import cloud.emusic.emotionmusicapi.domain.User;
import cloud.emusic.emotionmusicapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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


}
