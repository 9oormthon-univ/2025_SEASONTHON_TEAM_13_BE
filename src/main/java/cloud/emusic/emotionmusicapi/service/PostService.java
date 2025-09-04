package cloud.emusic.emotionmusicapi.service;

import cloud.emusic.emotionmusicapi.domain.*;
import cloud.emusic.emotionmusicapi.dto.response.EmotionTagResponse;
import cloud.emusic.emotionmusicapi.dto.request.PostCreateRequest;
import cloud.emusic.emotionmusicapi.dto.response.PostCreateResponse;
import cloud.emusic.emotionmusicapi.dto.response.PostResponseDto;
import cloud.emusic.emotionmusicapi.exception.CustomException;
import cloud.emusic.emotionmusicapi.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cloud.emusic.emotionmusicapi.exception.dto.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class PostService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
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

    public List<PostResponseDto> getAllPosts(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        return postRepository.findAll().stream()
                .map(post -> {
                            return getPostResponse(post,user);
                })
                .toList();
    }

    public PostResponseDto getPost(Long postId,Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        return getPostResponse(post,user);
    }

    @Transactional
    public PostResponseDto updatePost(Long postId, PostCreateRequest request, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new CustomException(INVALID_PERMISSION);
        }

        post.updateSongTrackId(request.getSongTrackId());

        // ===== 감정 태그 업데이트 =====
        Set<String> newEmotionTags = new HashSet<>(request.getEmotionTags());
        Set<String> oldEmotionTags = post.getEmotionTags().stream()
                .map(et -> et.getEmotionTag().getName())
                .collect(Collectors.toSet());

        // 제거할 태그
        oldEmotionTags.stream()
                .filter(tag -> !newEmotionTags.contains(tag))
                .forEach(post::removeEmotionTag);

        // 추가할 태그
        newEmotionTags.stream()
                .filter(tag -> !oldEmotionTags.contains(tag))
                .forEach(tag -> {
                    EmotionTag emotionTag = emotionTagRepository.findByName(tag)
                            .orElseThrow(() -> new CustomException(EMOTION_TAG_NOT_FOUND));
                    post.addEmotionTag(new PostEmotionTag(post, emotionTag));
                });

        // ===== 하루 태그 업데이트 (동일 로직) =====
        Set<String> newDayTags = new HashSet<>(request.getDailyTags());
        Set<String> oldDayTags = post.getDayTags().stream()
                .map(DayTag::getName)
                .collect(Collectors.toSet());

        oldDayTags.stream()
                .filter(tag -> !newDayTags.contains(tag))
                .forEach(post::removeDayTag);

        newDayTags.stream()
                .filter(tag -> !oldDayTags.contains(tag))
                .forEach(tag -> post.addDayTag(DayTag.create(tag, post)));

        // ===== 추가 정보 반환 =====
        long likeCount = likeRepository.countByPost(post);
        long commentCount = commentRepository.countByPost(post);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        boolean isLiked = likeRepository.existsByPostAndUser(post, user);

        return PostResponseDto.from(post, likeCount,isLiked,commentCount);
    }

    @Transactional
    public void deletePost(Long postId,Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new CustomException(INVALID_PERMISSION);
        }

        // 연관된 댓글과 좋아요 먼저 삭제
        commentRepository.deleteByPost(post);
        likeRepository.deleteByPost(post);

        postRepository.delete(post);
    }

    public List<PostResponseDto> getPostCalendar(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        return postRepository.findAllByUser(user).stream()
                .map(post -> getPostResponse(post,user))
                .toList();
    }

    private PostResponseDto getPostResponse(Post post,User user) {
        long likeCount = likeRepository.countByPost(post);
        long commentCount = commentRepository.countByPost(post);
        boolean isLiked = likeRepository.existsByPostAndUser(post,user);
        return PostResponseDto.from(post,likeCount,isLiked,commentCount);
    }
}
