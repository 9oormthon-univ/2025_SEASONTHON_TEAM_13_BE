package cloud.emusic.emotionmusicapi.service;

import cloud.emusic.emotionmusicapi.dto.request.EmotionMapper;
import cloud.emusic.emotionmusicapi.dto.response.TrackResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        // 1. 감정 → 장르 매핑
        List<String> genres = EmotionMapper.getGenres(emotions);
        String query = genres.stream()
                .map(g -> "genre:" + g)
                .collect(Collectors.joining(" OR "));
        query = "korean " + query + " year:2015-2025"; // 한국어 + 최신곡 조건

        List<TrackResponse> allResults = new ArrayList<>();

        // 2. 최대 200곡 가져오기 (50 * 4)
        for (int offset = 0; offset < 200; offset += 50) {
            String searchUrl = String.format(
                    "https://api.spotify.com/v1/search?q=%s&type=track&limit=50&offset=%d&market=KR",
                    URLEncoder.encode(query, StandardCharsets.UTF_8),
                    offset
            );

            ResponseEntity<String> searchResponse = restTemplate.exchange(
                    searchUrl, HttpMethod.GET, entity, String.class
            );

            try {
                JsonNode items = objectMapper.readTree(searchResponse.getBody())
                        .path("tracks").path("items");

                items.forEach(item -> {
                    String name = item.get("name").asText();
                    String releaseDate = item.path("album").path("release_date").asText();

                    // 3. 필터링: 한국어/숫자 제목 + 발매일 조건
                    if (!name.matches(".*[가-힣0-9].*")) return;
                    if (releaseDate.compareTo("2015-01-01") < 0) return;

                    String imageUrl = null;
                    JsonNode images = item.path("album").path("images");
                    if (images.isArray() && images.size() > 0) {
                        imageUrl = images.get(0).get("url").asText();
                    }

                    allResults.add(new TrackResponse(
                            item.get("id").asText(),
                            name,
                            item.path("artists").get(0).get("name").asText(),
                            item.path("external_urls").get("spotify").asText(),
                            imageUrl,
                            0.0, 0.0
                    ));
                });
            } catch (Exception e) {
                throw new RuntimeException("Search API 파싱 실패", e);
            }
        }

        // 4. 최종 결과: 요청한 개수만큼 반환
        return allResults.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }


    private String getAccessToken() {
        String url = "https://accounts.spotify.com/api/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret); // client_id:client_secret → Base64 자동 인코딩

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
        );

        return (String) response.getBody().get("access_token");
    }
}