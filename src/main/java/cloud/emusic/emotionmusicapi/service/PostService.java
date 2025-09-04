package cloud.emusic.emotionmusicapi.service;

import cloud.emusic.emotionmusicapi.domain.*;
import cloud.emusic.emotionmusicapi.dto.response.EmotionTagResponse;
import cloud.emusic.emotionmusicapi.dto.request.PostCreateRequest;
import cloud.emusic.emotionmusicapi.dto.response.PostCreateResponse;
import cloud.emusic.emotionmusicapi.dto.response.PostResponseDto;
import cloud.emusic.emotionmusicapi.exception.CustomException;
import cloud.emusic.emotionmusicapi.repository.EmotionTagRepository;
import cloud.emusic.emotionmusicapi.repository.LikeRepository;
import cloud.emusic.emotionmusicapi.repository.PostRepository;
import cloud.emusic.emotionmusicapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

import static cloud.emusic.emotionmusicapi.exception.dto.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class PostService {

    private final LikeRepository likeRepository;
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

    public List<PostResponseDto> getAllPosts() {
        return postRepository.findAll().stream()
                .map(post -> {
                            long likeCount = likeRepository.countByPost(post);
                            return PostResponseDto.from(post,likeCount);
                })
                .toList();
    }

    public PostResponseDto getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

        long likeCount = likeRepository.countByPost(post);

        return PostResponseDto.from(post,likeCount);
    }
}
