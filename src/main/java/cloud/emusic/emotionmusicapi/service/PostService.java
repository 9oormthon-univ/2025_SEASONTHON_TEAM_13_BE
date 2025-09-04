package cloud.emusic.emotionmusicapi.service;

import cloud.emusic.emotionmusicapi.domain.*;
import cloud.emusic.emotionmusicapi.dto.request.EmotionTagResponse;
import cloud.emusic.emotionmusicapi.dto.request.PostCreateRequest;
import cloud.emusic.emotionmusicapi.dto.request.PostCreateResponse;
import cloud.emusic.emotionmusicapi.exception.CustomException;
import cloud.emusic.emotionmusicapi.repository.EmotionTagRepository;
import cloud.emusic.emotionmusicapi.repository.PostRepository;
import cloud.emusic.emotionmusicapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import static cloud.emusic.emotionmusicapi.exception.dto.ErrorCode.EMOTION_TAG_NOT_FOUND;
import static cloud.emusic.emotionmusicapi.exception.dto.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final EmotionTagRepository emotionTagRepository;

    public List<EmotionTagResponse>EmotionTag() {
        return emotionTagRepository.findAll().stream()
                .map(tag -> new EmotionTagResponse(tag.getId(),tag.getName()))
                .toList();
    }

    @Transactional
    public PostCreateResponse createPost(Long userId, PostCreateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 2. Post 엔티티 생성
        Post post = new Post(user,request.getSongTrackId());

        // 3. 감정 태그 매핑 → PostEmotionTag 엔티티 생성
        request.getEmotionTags().forEach(tagName -> {
            EmotionTag emotionTag = emotionTagRepository.findByName(tagName)
                    .orElseThrow(() -> new CustomException(EMOTION_TAG_NOT_FOUND));
            post.addEmotionTag(new PostEmotionTag(post, emotionTag));
        });

        // 4. 하루 태그 매핑 → DayTag 엔티티 생성
        request.getDailyTags().forEach(tagName -> {
            DayTag dayTag = DayTag.create(tagName,post);
            post.addDayTag(dayTag);
        });

        postRepository.save(post);

        return new PostCreateResponse(post.getId());
    }
}
