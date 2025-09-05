package cloud.emusic.emotionmusicapi.service;

import cloud.emusic.emotionmusicapi.dto.response.TagRankingResponse;
import cloud.emusic.emotionmusicapi.dto.response.TagRankingResponseWrapper;
import cloud.emusic.emotionmusicapi.repository.DayTagRepository;
import cloud.emusic.emotionmusicapi.repository.PostEmotionTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final PostEmotionTagRepository postEmotionTagRepository;
    private final DayTagRepository dayTagRepository;

    public TagRankingResponseWrapper getTagRankings() {

        List<TagRankingResponse> postEmotionTagRankings = postEmotionTagRepository
                .findTopEmotionTags(PageRequest.of(0,10)).stream()
                .map(row -> new TagRankingResponse((String) row[0], (long) ((Number) row[1]).intValue())).toList();

        List<TagRankingResponse> dayTagRankings = dayTagRepository
                .findTopDayTags(PageRequest.of(0,10)).stream()
                .map(row -> new TagRankingResponse((String) row[0], (long) ((Number) row[1]).intValue())).toList();

        TagRankingResponseWrapper wrapper = new TagRankingResponseWrapper();
        wrapper.setEmotionTags(postEmotionTagRankings);
        wrapper.setDayTags(dayTagRankings);

        return wrapper;
    }
}
