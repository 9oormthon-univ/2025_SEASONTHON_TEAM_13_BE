package cloud.emusic.emotionmusicapi.service;

import cloud.emusic.emotionmusicapi.domain.Post;
import cloud.emusic.emotionmusicapi.domain.Song;
import cloud.emusic.emotionmusicapi.dto.response.EmotionTrackGroupResponse;
import cloud.emusic.emotionmusicapi.dto.response.TagRankingResponse;
import cloud.emusic.emotionmusicapi.dto.response.TagRankingResponseWrapper;
import cloud.emusic.emotionmusicapi.dto.response.TrackUsageResponse;
import cloud.emusic.emotionmusicapi.repository.DayTagRepository;
import cloud.emusic.emotionmusicapi.repository.PostEmotionTagRepository;
import cloud.emusic.emotionmusicapi.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final PostEmotionTagRepository postEmotionTagRepository;
    private final DayTagRepository dayTagRepository;
    private final PostRepository postRepository;

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

    public List<EmotionTrackGroupResponse> getTracksByEmotion() {
        List<Post> posts = postRepository.findAllWithTrackAndTags();

        // 감정 태그별 (곡 → count) 그룹화
        Map<String, Map<Song, Long>> grouped = posts.stream()
                .flatMap(post -> post.getEmotionTags().stream()
                        .map(tag -> Map.entry(tag.getEmotionTag().getName(), post.getSong())))
                .collect(Collectors.groupingBy(
                        e -> e.getKey(),
                        Collectors.groupingBy(e -> e.getValue(), Collectors.counting())
                ));

        // count 기준 정렬 후 DTO 변환
        return grouped.entrySet().stream()
                .map(entry -> {
                    String tagName = entry.getKey();
                    List<TrackUsageResponse> sortedTracks = entry.getValue().entrySet().stream()
                            .sorted(Map.Entry.<Song, Long>comparingByValue().reversed())
                            .map(e -> {
                                Song song = e.getKey();
                                return new TrackUsageResponse(
                                        song.getTrackId(),
                                        song.getTitle(),
                                        song.getArtist(),
                                        song.getAlbumArtUrl(),
                                        e.getValue(),
                                        song.getPlayCount()
                                );
                            })
                            .toList();
                    return new EmotionTrackGroupResponse(tagName,sortedTracks.size(),sortedTracks);
                })
                .toList();
    }
}
