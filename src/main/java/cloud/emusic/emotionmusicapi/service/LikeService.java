package cloud.emusic.emotionmusicapi.service;

import cloud.emusic.emotionmusicapi.domain.PostLike;
import cloud.emusic.emotionmusicapi.domain.Post;
import cloud.emusic.emotionmusicapi.domain.User;
import cloud.emusic.emotionmusicapi.exception.CustomException;
import cloud.emusic.emotionmusicapi.exception.dto.ErrorCode;
import cloud.emusic.emotionmusicapi.repository.LikeRepository;
import cloud.emusic.emotionmusicapi.repository.PostRepository;
import cloud.emusic.emotionmusicapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

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
}
