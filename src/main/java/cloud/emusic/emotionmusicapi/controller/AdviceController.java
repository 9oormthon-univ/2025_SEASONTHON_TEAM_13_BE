package cloud.emusic.emotionmusicapi.controller;

import cloud.emusic.emotionmusicapi.dto.response.AdviceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import service.AdviceService;

@Controller
@RequestMapping("/api/advice")
public class AdviceController {
    private final AdviceService adviceService;


    @Operation(summary = "랜덤 명언 조회", description = "현재 시간을 기반으로 랜덤 명언을 조회합니다.")
    @GetMapping
    public ResponseEntity<AdviceResponse> getAdvice(){
        AdviceResponse advice = adviceService.getRandomAdvice().block();
        if (advice == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(advice);
    }




}
