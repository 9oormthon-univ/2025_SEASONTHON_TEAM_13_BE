package cloud.emusic.emotionmusicapi.service;

import cloud.emusic.emotionmusicapi.domain.PostLike;
import cloud.emusic.emotionmusicapi.domain.Post;
import cloud.emusic.emotionmusicapi.domain.User;
import cloud.emusic.emotionmusicapi.dto.response.PostResponseDto;
import cloud.emusic.emotionmusicapi.exception.CustomException;
import cloud.emusic.emotionmusicapi.exception.dto.ErrorCode;
import cloud.emusic.emotionmusicapi.repository.CommentRepository;
import cloud.emusic.emotionmusicapi.repository.LikeRepository;
import cloud.emusic.emotionmusicapi.repository.PostRepository;
import cloud.emusic.emotionmusicapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void likePost(Long postId,Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()->new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(()->new CustomException(ErrorCode.POST_NOT_FOUND));

        likeRepository.findByUserAndPost(user, post).ifPresent(like -> {
            throw new CustomException(ErrorCode.ALREADY_LIKED);
        });

        likeRepository.save(PostLike.create(user, post));
    }

    @Transactional
    public void unLikePost(Long postId,Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()->new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(()->new CustomException(ErrorCode.POST_NOT_FOUND));

        PostLike like = likeRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new CustomException(ErrorCode.LIKE_NOT_FOUND));

        likeRepository.delete(like);
    }

    public List<PostResponseDto> getLikedPosts(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()->new CustomException(ErrorCode.USER_NOT_FOUND));

        List<PostLike> likes = likeRepository.findAllByUser(user);

        return likes.stream()
                .map(like ->
                        {
                            Post post = like.getPost();
                            long likeCount = likeRepository.countByPost(post);
                            long commentCount = commentRepository.countByPost(post);
                            boolean isLiked = likeRepository.existsByPostIdAndUserId(post.getId(), userId);
                            return PostResponseDto.from(post,likeCount,isLiked,commentCount);
                        })
                .collect(Collectors.toList());
    }
}
