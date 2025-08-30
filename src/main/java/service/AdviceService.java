package service;

import cloud.emusic.emotionmusicapi.dto.response.AdviceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AdviceService {

    private final WebClient webClient;

    private String adviceApiUrl = "https://korean-advice-open-api.vercel.app/api/advice";

    /**
     * 외부 명언 API를 비동기적으로 호출하여 랜덤 명언을 가져옵니다.
     * @param time 현재 시간 (Linux time)
     * @return Mono<AdviceResponse> - 비동기 응답 스트림
     */
    public Mono<AdviceResponse> getRandomAdvice() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(adviceApiUrl)
                        .build())
                .retrieve()
                .bodyToMono(AdviceResponse.class);
    }

}
