package cloud.emusic.emotionmusicapi.controller;

import cloud.emusic.emotionmusicapi.config.JwtTokenProvider;
import cloud.emusic.emotionmusicapi.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/temp-token")
@RequiredArgsConstructor
public class TempTokenController {

  private final JwtTokenProvider jwtTokenProvider;

  @GetMapping
  public ResponseEntity<String> issueTempToken(@RequestParam Long userId) {
    String token = jwtTokenProvider.createAccessToken(userId, Role.USER);
    return ResponseEntity.ok(token);
  }
}