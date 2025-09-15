package cloud.emusic.emotionmusicapi.service;

import cloud.emusic.emotionmusicapi.domain.post.Post;
import cloud.emusic.emotionmusicapi.domain.song.Song;
import cloud.emusic.emotionmusicapi.domain.tag.DayTag;
import cloud.emusic.emotionmusicapi.domain.tag.EmotionTag;
import cloud.emusic.emotionmusicapi.domain.tag.PostEmotionTag;
import cloud.emusic.emotionmusicapi.domain.user.User;
import cloud.emusic.emotionmusicapi.dto.request.PostCreateRequest;
import cloud.emusic.emotionmusicapi.dto.response.tag.EmotionTagResponse;
import cloud.emusic.emotionmusicapi.dto.response.post.PostCreateResponse;
import cloud.emusic.emotionmusicapi.dto.response.post.PostResponse;
import cloud.emusic.emotionmusicapi.dto.response.song.TrackResponse;
import cloud.emusic.emotionmusicapi.exception.CustomException;
import cloud.emusic.emotionmusicapi.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
        // DB에 저장된 모든 감정 태그를 조회하여 DTO로 변환 후 반환
        // 조회 리스트를 stream으로 변환하여 map을 통해 EmotionTagResponse로 매핑
        // 최종적으로 toList()를 통해 리스트로 수집하여 반환
        try {
            return emotionTagRepository.findAll().stream()
                    .map(tag -> new EmotionTagResponse(tag.getId(), tag.getName()))
                    .toList();
        } catch (Exception e) {
            throw new CustomException(EMOTION_TAGS_NOT_FOUND);
        }
    }

    @Transactional
    public PostCreateResponse createPost(Long userId, PostCreateRequest request) {

        // userId로 User 엔티티 조회, 없으면 예외 발생
        // Optional의 orElseThrow 메서드를 사용하여 User 엔티티를 가져오거나 예외를 던짐
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // request의 songTrackId로 Song 엔티티 조회
        // orElseGet을 사용하여 없으면 Spotify API 호출 후 새 Song 생성 및 저장
        Song song = songRepository.findByTrackId(request.getSongTrackId())
            .orElseGet(() -> {

                // SpotifyService를 통해 트랙 정보 조회
                TrackResponse trackInfo = spotifyService.getTrackById(request.getSongTrackId());

                Song newSong = Song.builder()
                    .trackId(trackInfo.getTrackId())
                    .title(trackInfo.getName())
                    .artist(trackInfo.getArtist())
                    .albumArtUrl(trackInfo.getImageUrl())
                    .spotifyPlayUrl(trackInfo.getSpotifyUrl())
                    .build();

                return songRepository.save(newSong);
            });

        Post post = new Post(user, song);

        // 감정 태그 Post에 추가
        // forEach를 사용하여 요청된 각 태그 이름에 대해 DB에서 조회 후 Post에 추가
        // 일치하는 태그가 없으면 예외 발생
        request.getEmotionTags().forEach(tagName -> {
            EmotionTag emotionTag = emotionTagRepository.findByName(tagName)
                .orElseThrow(() -> new CustomException(EMOTION_TAG_NOT_FOUND));
            post.addEmotionTag(new PostEmotionTag(post, emotionTag));
        });

        // 하루 태그를 Post에 추가
        request.getDailyTags().forEach(tagName -> {
            DayTag dayTag = DayTag.create(tagName, post);
            post.addDayTag(dayTag);
        });

        postRepository.save(post);

        return new PostCreateResponse(post.getId());
    }

    // 게시글 목록 조회
    public List<PostResponse> getAllPosts(Long userId, String sortBy, String direction, int page, int size) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // likeCount로 정렬하는 경우, 커스텀 쿼리 사용
        if (sortBy.equals("likeCount")) {

            // 페이지네이션을 위해 offset 계산
            return postRepository.findPostsOrderByLikeCount(size, page * size).stream()
                .map(result -> {

                    // 결과 배열에서 postId 추출 및 Post 엔티티 조회
                    Long postId = ((Number) result[0]).longValue();
                    Post post = postRepository.findById(postId)
                        .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

                    // 좋아요 수, 댓글 수, 사용자가 좋아요했는지 여부 계산
                    Long likeCount = ((Number) result[result.length - 1]).longValue();
                    Long commentCount = commentRepository.countByPost(post);

                    // 엔티티로 비교하는 방법은 안정적이지 않아 ID로 비교
                    boolean isLiked = likeRepository.existsByPostIdAndUserId(post.getId(), user.getId());

                    return PostResponse.from(post, likeCount, isLiked, commentCount);
                }).toList();
        } else {
            // 기본 정렬 (createdAt, updatedAt 등)인 경우, Spring Data JPA의 페이징 및 정렬 기능 사용
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));

            // Post 엔티티를 페이지 단위로 조회 후, 각 게시글에 대해 좋아요 수, 댓글 수, 사용자가 좋아요했는지 여부 계산
            return postRepository.findAll(pageable).stream()
                .map(post -> {
                    long likeCount = likeRepository.countByPost(post);
                    long commentCount = commentRepository.countByPost(post);
                    boolean isLiked = likeRepository.existsByPostIdAndUserId(post.getId(), user.getId());
                    return PostResponse.from(post, likeCount, isLiked, commentCount);
                })
                .toList();
        }
    }

    public PostResponse getPost(Long postId, Long userId) {

        // 게시글과 유저 엔티티를 ID로 조회하여 존재하지 않으면 예외 발생 있다면 DTO 변환후 반환
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        return getPostResponse(post, user);
    }

    @Transactional
    public PostResponse updatePost(Long postId, PostCreateRequest request, Long userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

        // 게시글 작성자와 요청한 유저가 같은지 확인
        if (!post.getUser().getId().equals(userId)) {
            throw new CustomException(INVALID_PERMISSION);
        }

        // 노래 변경 시, 새로운 노래로 업데이트
        if (!post.getSong().getTrackId().equals(request.getSongTrackId())) {
            Song song = songRepository.findByTrackId(request.getSongTrackId())
                .orElseGet(() -> {
                    TrackResponse trackInfo = spotifyService.getTrackById(request.getSongTrackId());
                    Song newSong = Song.builder()
                        .trackId(trackInfo.getTrackId())
                        .title(trackInfo.getName())
                        .artist(trackInfo.getArtist())
                        .albumArtUrl(trackInfo.getImageUrl())
                        .spotifyPlayUrl(trackInfo.getSpotifyUrl())
                        .build();
                    return songRepository.save(newSong);
                });
            post.updateSong(song);
        }

        // Set 자료구조로 변경 전후 태그 비교 후 추가/삭제
        Set<String> newEmotionTags = new HashSet<>(request.getEmotionTags());

        // 기존 태그 이름만 추출하여 Set으로 변환
        Set<String> oldEmotionTags = post.getEmotionTags().stream()
            .map(et -> et.getEmotionTag().getName())
            .collect(Collectors.toSet());

        // 기존 태그 중 새로운 태그에 없는 것들은 삭제
        // filter 로 새로운 태그에 없는 기존 태그만 걸러내어 삭제
        oldEmotionTags.stream()
            .filter(tag -> !newEmotionTags.contains(tag))
            .forEach(post::removeEmotionTag);

        // 새로운 태그 중 기존 태그에 없는 것들은 추가
        newEmotionTags.stream()
            .filter(tag -> !oldEmotionTags.contains(tag))
            .forEach(tag -> {
                EmotionTag emotionTag = emotionTagRepository.findByName(tag)
                    .orElseThrow(() -> new CustomException(EMOTION_TAG_NOT_FOUND));
                post.addEmotionTag(new PostEmotionTag(post, emotionTag));
            });

        // 하루 태그도 감정 태그와 비슷한 방법으로 처리
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

        // 부가 정보 조회 및 DTO 변환 후 반환
        long likeCount = likeRepository.countByPost(post);
        long commentCount = commentRepository.countByPost(post);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        boolean isLiked = likeRepository.existsByPostIdAndUserId(post.getId(), user.getId());

        return PostResponse.from(post, likeCount, isLiked, commentCount);
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

    public List<PostResponse> getPostCalendar(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 유저 조회후 해당 유저의 모든 게시글 조회 및 DTO 변환 후 반환
        return postRepository.findAllByUser(user).stream()
            .map(post -> getPostResponse(post, user))
            .toList();
    }

    public PostResponse getMyPost (Long userId, LocalDate createdDate) {

        // createdDate의 시작과 끝 시각 계산
        LocalDateTime start = createdDate.atStartOfDay();
        LocalDateTime end = createdDate.plusDays(1).atStartOfDay();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Post post = postRepository.findByUserAndCreatedDate(user,start,end).orElse(null);

        // 해당 날짜에 작성된 게시글이 없으면 null 반환
        return post != null ? getPostResponse(post, user) : null;
    }

    public List<PostResponse> searchPostsTag(Long userId, String tag, int page, int size) {

        // Pageable 객체 생성, 생성일자 내림차순 정렬
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // pageable을 사용하여 태그로 게시글 검색
        // 감정,하루 태그 모두 포함
        Page<Post> posts = postRepository.searchPostsByTag(tag, pageable);

        return posts.stream()
                .map(post -> {
                    long likeCount = likeRepository.countByPost(post);
                    long commentCount = commentRepository.countByPost(post);
                    boolean isLiked = likeRepository.existsByPostIdAndUserId(post.getId(), userId);
                    return PostResponse.from(post, likeCount, isLiked, commentCount);
                }).toList();
    }

    // 공통 로직 메서드화
    private PostResponse getPostResponse(Post post, User user) {
        long likeCount = likeRepository.countByPost(post);
        long commentCount = commentRepository.countByPost(post);
        boolean isLiked = likeRepository.existsByPostIdAndUserId(post.getId(), user.getId());
        return PostResponse.from(post, likeCount, isLiked, commentCount);
    }
}