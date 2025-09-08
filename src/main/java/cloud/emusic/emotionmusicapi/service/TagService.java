package cloud.emusic.emotionmusicapi.service;

import cloud.emusic.emotionmusicapi.domain.post.Post;
import cloud.emusic.emotionmusicapi.domain.song.Song;
import cloud.emusic.emotionmusicapi.dto.response.tag.EmotionTrackGroupResponse;
import cloud.emusic.emotionmusicapi.dto.response.tag.TagRankingResponse;
import cloud.emusic.emotionmusicapi.dto.response.tag.TagRankingResponseWrapper;
import cloud.emusic.emotionmusicapi.dto.response.song.TrackUsageResponse;
import cloud.emusic.emotionmusicapi.repository.DayTagRepository;
import cloud.emusic.emotionmusicapi.repository.PostEmotionTagRepository;
import cloud.emusic.emotionmusicapi.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByValue;

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

    public List<EmotionTrackGroupResponse> getTracksByEmotion(String tagName) {
        List<Post> posts = postRepository.findAllWithTrackAndTags();

        // 감정 태그별 (곡 → count) 그룹화
        Map<Song, Long> songCountMap = posts.stream()
                .flatMap(post -> post.getEmotionTags().stream()
                        .filter(tag -> tag.getEmotionTag().getName().equals(tagName))
                        .map(tag -> post.getSong()))
                .collect(Collectors.groupingBy(song -> song, Collectors.counting()));

        List<TrackUsageResponse> sortedTracks = songCountMap.entrySet().stream()
                .sorted(Map.Entry.<Song, Long>comparingByValue().reversed())
                .map(e -> {
                    Song song = e.getKey();
                    Long count = e.getValue();
                    return new TrackUsageResponse(
                            song.getTrackId(),
                            song.getTitle(),
                            song.getArtist(),
                            song.getAlbumArtUrl(),
                            count,
                            song.getPlayCount()
                    );
                })
                .toList();

        return Collections.singletonList(new EmotionTrackGroupResponse(tagName,sortedTracks.size(), sortedTracks));

    }
}
