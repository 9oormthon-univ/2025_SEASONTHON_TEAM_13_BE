package cloud.emusic.emotionmusicapi.service;

import cloud.emusic.emotionmusicapi.domain.Song;
import cloud.emusic.emotionmusicapi.dto.request.EmotionMapper;
import cloud.emusic.emotionmusicapi.dto.response.TrackResponse;
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

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final SongRepository songRepository;

    @Value("${SPOTIFY_CLIENT_ID}")
    private String clientId;

    @Value("${SPOTIFY_CLIENT_SECRET}")
    private String clientSecret;

    public List<TrackResponse> recommendByEmotions(List<String> emotions, int limit) {
        String accessToken = getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        List<String> genres = EmotionMapper.getGenres(emotions);
        String query = genres.stream().map(g -> "genre:" + g).collect(Collectors.joining(" OR "));
        query = "korean " + query + " year:2015-2025";

        List<TrackResponse> allResults = new ArrayList<>();

        for (int offset = 0; offset < 200; offset += 50) {
            String searchUrl = String.format(
                "https://api.spotify.com/v1/search?q=%s&type=track&limit=50&offset=%d&market=KR",
                URLEncoder.encode(query, StandardCharsets.UTF_8),
                offset
            );

            ResponseEntity<String> searchResponse = restTemplate.exchange(searchUrl, HttpMethod.GET, entity, String.class);

            try {
                JsonNode items = objectMapper.readTree(searchResponse.getBody()).path("tracks").path("items");
                items.forEach(item -> {
                    String name = item.get("name").asText();
                    JsonNode albumNode = item.path("album");
                    String releaseDate = albumNode.path("release_date").asText();

                    if (!name.matches(".*[가-힣0-9].*") || releaseDate.compareTo("2015-01-01") < 0) return;

                    String imageUrl = null;
                    JsonNode images = albumNode.path("images");
                    if (images.isArray() && !images.isEmpty()) {
                        imageUrl = images.get(0).get("url").asText();
                    }

                    allResults.add(new TrackResponse(
                        item.get("id").asText(),
                        name,
                        item.path("artists").get(0).get("name").asText(),
                        item.path("external_urls").get("spotify").asText(),
                        imageUrl,
                        albumNode.path("name").asText(null), // 앨범명 추가
                        releaseDate,                       // 발매일 추가
                        0.0,
                        0.0
                    ));
                });
            } catch (Exception e) {
                throw new RuntimeException("Search API 파싱 실패", e);
            }
        }
        return allResults.stream().limit(limit).collect(Collectors.toList());
    }

    public TrackResponse getTrackById(String trackId) {
        String accessToken = getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = "https://api.spotify.com/v1/tracks/" + trackId;

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
                releaseDate,
                0.0,
                0.0
            );
        } catch (Exception e) {
            log.error("Spotify API getTrackById 실패: trackId={}, error={}", trackId, e.getMessage());
            throw new CustomException(ErrorCode.SPOTIFY_API_ERROR);
        }
    }

    public List<TrackResponse> searchTracksByTitleKorean(String keyword, int limit) {
        String accessToken = getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String query = "track:" + keyword;
        List<TrackResponse> allResults = new ArrayList<>();

        try {
            for (int offset = 0; offset < 200; offset += 50) {
                String searchUrl = String.format("https://api.spotify.com/v1/search?q=%s&type=track&limit=50&offset=%d&market=KR", query, offset);
                ResponseEntity<String> searchResponse = restTemplate.exchange(searchUrl, HttpMethod.GET, entity, String.class);
                JsonNode items = objectMapper.readTree(searchResponse.getBody()).path("tracks").path("items");

                String[] tokens = keyword.replaceAll("\\s+", "").split("");

                items.forEach(item -> {
                    String name = item.get("name").asText();
                    if (Arrays.stream(tokens).allMatch(name::contains)) {
                        JsonNode albumNode = item.path("album");
                        String imageUrl = null;
                        JsonNode images = albumNode.path("images");
                        if (images.isArray() && !images.isEmpty()) {
                            imageUrl = images.get(0).get("url").asText();
                        }

                        allResults.add(new TrackResponse(
                            item.get("id").asText(),
                            name,
                            item.path("artists").get(0).get("name").asText(),
                            item.path("external_urls").get("spotify").asText(),
                            imageUrl,
                            albumNode.path("name").asText(null), // 앨범명 추가
                            albumNode.path("release_date").asText(null), // 발매일 추가
                            0.0,
                            0.0
                        ));
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Search API 파싱 실패", e);
        }
        return allResults.stream().limit(limit).collect(Collectors.toList());
    }

    @Transactional
    public void songCountUp(String trackId){
        Song song = songRepository.findByTrackId(trackId)
                        .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        song.plusPlayCount();

        songRepository.save(song);
    }

    private String getAccessToken() {
        String url = "https://accounts.spotify.com/api/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
        return (String) response.getBody().get("access_token");
    }
}