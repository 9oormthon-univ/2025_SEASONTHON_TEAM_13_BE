package cloud.emusic.emotionmusicapi.config;

import cloud.emusic.emotionmusicapi.domain.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${JWT_SECRET_KEY}")
    private String secretKeyString;

    private static final long JWT_EXPIRATION_TIME = 1000L * 60 * 60 * 24; // 24 hours

    // Key 객체로 변환한 서명용 비밀 키
    private Key key;

    // 빈 초기화 시점에 Key 객체 생성
    @PostConstruct
    protected void init() {
      // HMAC-SHA 알고리즘에 맞는 안전한 key 생성
      key = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, Role role) {
      return createToken(userId,role,JWT_EXPIRATION_TIME);
    }

    // JWT 토큰 생성 메서드
    public String createToken(Long userId,Role role, long expiration) {

      Date now = new Date();

      return Jwts.builder()
              .setSubject(String.valueOf(userId))                                 // 토큰 제목: email (고유 식별자)
              .claim("role",role.name())                                    // 사용자 역할을 커스텀 클레임으로 추가
              .setIssuedAt(now)                                                   // 발급 시각
              .setExpiration(new Date(System.currentTimeMillis() + expiration))   // 만료 시각 설정
              .signWith(key, SignatureAlgorithm.HS256)                            // 비밀 키로 서명 (HMAC-SHA256)
              .compact();                                                         // JWT 문자열 생성
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
      try {
        parseClaims(token);
        return true;
      } catch (JwtException | IllegalArgumentException e) {
        return false;
      }
    }

    // 토큰에서 사용자 이메일 추출
    public String getUserId(String token) {
      return parseClaims(token).getSubject();
    }

    // 토큰에서 역할(role) 추출
    public String getRole(String token) {
      return parseClaims(token).get("role", String.class);
    }

    // Claims 추출 (토큰 내부 정보 파싱)
    private Claims parseClaims(String token) {
      return Jwts.parserBuilder()
              .setSigningKey(key)
              .build()
              .parseClaimsJws(token)
              .getBody();
    }
}