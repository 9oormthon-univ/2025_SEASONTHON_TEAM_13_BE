package cloud.emusic.emotionmusicapi.service;

import cloud.emusic.emotionmusicapi.domain.song.Song;
import cloud.emusic.emotionmusicapi.dto.request.EmotionMapper;
import cloud.emusic.emotionmusicapi.dto.response.song.TrackResponse;
import cloud.emusic.emotionmusicapi.exception.CustomException;
import cloud.emusic.emotionmusicapi.exception.dto.ErrorCode;
import cloud.emusic.emotionmusicapi.repository.SongRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpotifyService {

    @Value("${SPOTIFY_CLIENT_ID}")
    private String spotifyClientId;

    @Value("${SPOTIFY_CLIENT_SECRET}")
    private String spotifyClientSecret;

    @Value("${LASTFM_API_KEY}")
    private String lastfmApiKey;

    private static final String LASTFM_API_URL = "https://ws.audioscrobbler.com/2.0/?method=tag.getTopTracks&tag=%s&limit=%d&api_key=%s&format=json";

    private static final String SPOTIFY_ACCESS_TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String SPOTIFY_TRACK_API_URL = "https://api.spotify.com/v1/tracks/";
    private static final String SPOTIFY_LAST_RELEASES_API_URL = "https://api.spotify.com/v1/search?q=%s&type=track&market=KR&limit=1";
    private static final String SPOTIFY_TITLE_SEARCH_API_URL = "https://api.spotify.com/v1/search?q=%s&type=track&market=KR&limit=%d&offset=%d";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SongRepository songRepository;

    // 감정 기반 노래 추천
    public List<TrackResponse> searchTracksByEmotion(List<String> emotions,int limit) {

        List<String> englishEmotion = EmotionMapper.getEnglishEmotions(emotions);

        // 고유 태그 추출
        Map<String, Long> emotionCount = englishEmotion.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        Map<String, List<TrackResponse>> tagToTracks = new HashMap<>();

        for (Map.Entry<String, Long> entry : emotionCount.entrySet()) {
            String emotion = entry.getKey();

            List<TrackResponse> tracks = searchTracksByEmotionTag(emotion, limit);
            tagToTracks.put(emotion, tracks);
        }

        // 라운드 로빈 + 중복 제거
        List<TrackResponse> results = new ArrayList<>();
        Set<String> seenTrackIds = new HashSet<>();

        int maxSize = tagToTracks.values().stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);

        outer:
        for (int i = 0; i < maxSize; i++) {
            for (String emotion : tagToTracks.keySet()) {
                List<TrackResponse> tracks = tagToTracks.get(emotion);
                if (tracks != null && i < tracks.size()) {
                    TrackResponse track = tracks.get(i);
                    if (track.getTrackId() != null && seenTrackIds.add(track.getTrackId())) {
                        results.add(track);

                        if (results.size() >= limit) {
                            break outer;
                        }
                    }
                }
            }
        }

        return results;
    }

    // 트랙 ID로 노래 정보 조회
    public TrackResponse getTrackById(String trackId) {

        HttpEntity<Void> entity = buildSpotifyHeaders();
        String url = SPOTIFY_TRACK_API_URL + trackId;

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode track = objectMapper.readTree(response.getBody());

            JsonNode albumNode = track.path("album");
            String albumName = albumNode.path("name").asText(null);
            String releaseDate = albumNode.path("release_date").asText(null);

            String imageUrl = null;
            JsonNode images = albumNode.path("images");
            if (images.isArray() && !images.isEmpty()) {
                imageUrl = images.get(0).get("url").asText();
            }

            return new TrackResponse(
                track.get("id").asText(),
                track.get("name").asText(),
                track.path("artists").get(0).get("name").asText(),
                track.path("external_urls").get("spotify").asText(),
                imageUrl,
                albumName,
                releaseDate
            );
        } catch (Exception e) {
            log.error("Spotify API getTrackById 실패: trackId={}, error={}", trackId, e.getMessage());
            throw new CustomException(ErrorCode.SPOTIFY_API_ERROR);
        }
    }

    // 곡명으로 노래 검색
    public List<TrackResponse> searchTracksByTitle(String keyword,int limit) {

        HttpEntity<Void> entity = buildSpotifyHeaders();
        List<TrackResponse> results = new ArrayList<>();

        try {
            String searchUrl = String.format(SPOTIFY_TITLE_SEARCH_API_URL, keyword,limit, 0);
            ResponseEntity<String> searchResponse = restTemplate.exchange(searchUrl, HttpMethod.GET, entity, String.class);
            JsonNode items = objectMapper.readTree(searchResponse.getBody())
                    .path("tracks")
                    .path("items");

            for (JsonNode item : items) {
                String name = item.get("name").asText();

                JsonNode albumNode = item.path("album");

                String imageUrl = null;
                JsonNode images = albumNode.path("images");
                if (images.isArray() && !images.isEmpty()) {
                    imageUrl = images.get(0).get("url").asText();
                }

                results.add(new TrackResponse(
                        item.get("id").asText(),
                        name,
                        item.path("artists").get(0).get("name").asText(),
                        item.path("external_urls").get("spotify").asText(),
                        imageUrl,
                        albumNode.path("name").asText(null),
                        albumNode.path("release_date").asText(null)
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Search API 파싱 실패", e);
        }
        return results;
    }

    @Transactional
    public void songCountUp(String trackId){

        Song song = songRepository.findByTrackId(trackId)
                        .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        song.plusPlayCount();
        songRepository.save(song);
    }

    private String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(spotifyClientId, spotifyClientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(SPOTIFY_ACCESS_TOKEN_URL, HttpMethod.POST, request, Map.class);
        return (String) response.getBody().get("access_token");
    }

    // 감정 기반 노래 추천 - Spotify에서 노래 검색
    private List<TrackResponse> searchTracksByEmotionTag(String emotion, int limit) {

        HttpEntity<Void> entity = buildSpotifyHeaders();

        String url = String.format(
                LASTFM_API_URL,
                emotion, limit, lastfmApiKey
        );

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        List<Map<String, Object>> tracks = (List<Map<String, Object>>)
                ((Map<String, Object>) response.get("tracks")).get("track");

        List<TrackResponse> results = tracks.parallelStream()
                .map(track -> {
                    String songTitle = (String) track.get("name");
                    try {
                        String searchUrl = String.format(
                                SPOTIFY_LAST_RELEASES_API_URL,
                                URLEncoder.encode(songTitle, StandardCharsets.UTF_8)
                        );

                        ResponseEntity<String> searchResponse = restTemplate.exchange(searchUrl, HttpMethod.GET, entity, String.class);
                        JsonNode items = objectMapper.readTree(searchResponse.getBody())
                                .path("tracks").path("items");

                        if (items.isArray() && items.size() > 0) {
                            JsonNode item = items.get(0);
                            JsonNode albumNode = item.path("album");

                            String imageUrl = null;
                            JsonNode images = albumNode.path("images");
                            if (images.isArray() && !images.isEmpty()) {
                                imageUrl = images.get(0).get("url").asText();
                            }

                            return new TrackResponse(
                                    item.get("id").asText(),
                                    item.get("name").asText(),
                                    item.path("artists").get(0).get("name").asText(),
                                    item.path("external_urls").get("spotify").asText(),
                                    imageUrl,
                                    albumNode.path("name").asText(null),
                                    albumNode.path("release_date").asText(null)
                            );
                        }
                    } catch (Exception e) {
                        log.warn("Spotify 검색 실패: {}", songTitle, e);
                    }
                    return null; // 실패 시 null 반환
                })
                .filter(Objects::nonNull)
                .toList();
        return results;
    }

    private HttpEntity<Void> buildSpotifyHeaders() {
        String accessToken = getAccessToken();
        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return new HttpEntity<>(headers);
    }
}