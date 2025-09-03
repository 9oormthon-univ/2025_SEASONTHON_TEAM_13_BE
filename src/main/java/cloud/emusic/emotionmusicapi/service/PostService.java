package cloud.emusic.emotionmusicapi.service;

import cloud.emusic.emotionmusicapi.dto.request.EmotionTagResponse;
import cloud.emusic.emotionmusicapi.repository.EmotionTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final EmotionTagRepository emotionTagRepository;

    public List<EmotionTagResponse>EmotionTag() {
        return emotionTagRepository.findAll().stream()
                .map(tag -> new EmotionTagResponse(tag.getId(),tag.getName()))
                .toList();
    }
}
