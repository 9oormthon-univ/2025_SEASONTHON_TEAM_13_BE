package cloud.emusic.emotionmusicapi.service;

import cloud.emusic.emotionmusicapi.domain.*;
import cloud.emusic.emotionmusicapi.dto.request.PostCreateRequest;
import cloud.emusic.emotionmusicapi.dto.response.EmotionTagResponse;
import cloud.emusic.emotionmusicapi.dto.response.PostCreateResponse;
import cloud.emusic.emotionmusicapi.dto.response.PostResponseDto;
import cloud.emusic.emotionmusicapi.dto.response.TrackResponse;
import cloud.emusic.emotionmusicapi.exception.CustomException;
import cloud.emusic.emotionmusicapi.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final SongRepository songRepository;
    private final SpotifyService spotifyService;

    public List<EmotionTagResponse> EmotionTag() {
        return emotionTagRepository.findAll().stream()
            .map(tag -> new EmotionTagResponse(tag.getId(), tag.getName()))
            .toList();
    }

    @Transactional
    public PostCreateResponse createPost(Long userId, PostCreateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Song song = songRepository.findByTrackId(request.getSongTrackId())
            .orElseGet(() -> {
                TrackResponse trackInfo = spotifyService.getTrackById(request.getSongTrackId());

                Song newSong = Song.builder()
                    .trackId(trackInfo.getId())
                    .title(trackInfo.getName())
                    .artist(trackInfo.getArtist())
                    .albumArtUrl(trackInfo.getImageUrl())
                    .spotifyPlayUrl(trackInfo.getSpotifyUrl())
                    .build();

                return songRepository.save(newSong);
            });

        Post post = new Post(user, song);

        request.getEmotionTags().forEach(tagName -> {
            EmotionTag emotionTag = emotionTagRepository.findByName(tagName)
                .orElseThrow(() -> new CustomException(EMOTION_TAG_NOT_FOUND));
            post.addEmotionTag(new PostEmotionTag(post, emotionTag));
        });

        request.getDailyTags().forEach(tagName -> {
            DayTag dayTag = DayTag.create(tagName, post);
            post.addDayTag(dayTag);
        });

        postRepository.save(post);

        return new PostCreateResponse(post.getId());
    }

    public List<PostResponseDto> getAllPosts(Long userId, String sortBy, String direction, int page, int size) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (sortBy.equals("likeCount")) {
            return postRepository.findPostsOrderByLikeCount(size, page * size).stream()
                .map(result -> {
                    Long postId = ((Number) result[0]).longValue();
                    Post post = postRepository.findById(postId)
                        .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

                    Long likeCount = ((Number) result[result.length - 1]).longValue();
                    Long commentCount = commentRepository.countByPost(post);
                    boolean isLiked = likeRepository.existsByPostIdAndUserId(post.getId(), user.getId());

                    return PostResponseDto.from(post, likeCount, isLiked, commentCount);
                }).toList();
        } else {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
            return postRepository.findAll(pageable).stream()
                .map(post -> {
                    long likeCount = likeRepository.countByPost(post);
                    long commentCount = commentRepository.countByPost(post);
                    boolean isLiked = likeRepository.existsByPostIdAndUserId(post.getId(), user.getId());
                    return PostResponseDto.from(post, likeCount, isLiked, commentCount);
                })
                .toList();
        }
    }

    public PostResponseDto getPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        return getPostResponse(post, user);
    }

    @Transactional
    public PostResponseDto updatePost(Long postId, PostCreateRequest request, Long userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new CustomException(INVALID_PERMISSION);
        }

        if (!post.getSong().getTrackId().equals(request.getSongTrackId())) {
            Song song = songRepository.findByTrackId(request.getSongTrackId())
                .orElseGet(() -> {
                    TrackResponse trackInfo = spotifyService.getTrackById(request.getSongTrackId());
                    Song newSong = Song.builder()
                        .trackId(trackInfo.getId())
                        .title(trackInfo.getName())
                        .artist(trackInfo.getArtist())
                        .albumArtUrl(trackInfo.getImageUrl())
                        .spotifyPlayUrl(trackInfo.getSpotifyUrl())
                        .build();
                    return songRepository.save(newSong);
                });
            post.updateSong(song);
        }

        Set<String> newEmotionTags = new HashSet<>(request.getEmotionTags());
        Set<String> oldEmotionTags = post.getEmotionTags().stream()
            .map(et -> et.getEmotionTag().getName())
            .collect(Collectors.toSet());

        oldEmotionTags.stream()
            .filter(tag -> !newEmotionTags.contains(tag))
            .forEach(post::removeEmotionTag);

        newEmotionTags.stream()
            .filter(tag -> !oldEmotionTags.contains(tag))
            .forEach(tag -> {
                EmotionTag emotionTag = emotionTagRepository.findByName(tag)
                    .orElseThrow(() -> new CustomException(EMOTION_TAG_NOT_FOUND));
                post.addEmotionTag(new PostEmotionTag(post, emotionTag));
            });

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

        long likeCount = likeRepository.countByPost(post);
        long commentCount = commentRepository.countByPost(post);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        boolean isLiked = likeRepository.existsByPostIdAndUserId(post.getId(), user.getId());

        return PostResponseDto.from(post, likeCount, isLiked, commentCount);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new CustomException(INVALID_PERMISSION);
        }

        commentRepository.deleteByPost(post);
        likeRepository.deleteByPost(post);

        postRepository.delete(post);
    }

    public List<PostResponseDto> getPostCalendar(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        return postRepository.findAllByUser(user).stream()
            .map(post -> getPostResponse(post, user))
            .toList();
    }

    public PostResponseDto getMyPost (Long userId, LocalDate createdDate) {

        LocalDateTime start = createdDate.atStartOfDay();
        LocalDateTime end = createdDate.plusDays(1).atStartOfDay();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Post post = postRepository.findByUserAndCreatedDate(user,start,end).orElse(null);

        return post != null ? getPostResponse(post, user) : null;
    }

    private PostResponseDto getPostResponse(Post post, User user) {
        long likeCount = likeRepository.countByPost(post);
        long commentCount = commentRepository.countByPost(post);
        boolean isLiked = likeRepository.existsByPostIdAndUserId(post.getId(), user.getId());
        return PostResponseDto.from(post, likeCount, isLiked, commentCount);
    }
}