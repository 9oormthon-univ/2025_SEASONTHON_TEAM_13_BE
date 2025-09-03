package cloud.emusic.emotionmusicapi.config;

import cloud.emusic.emotionmusicapi.domain.User;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  @Value("${JWT_SECRET_KEY}")
  private String secretKeyString;

  private SecretKey secretKey;

  private static final long JWT_EXPIRATION_TIME = 1000L * 60 * 60 * 24; // 24 hours

  @PostConstruct
  public void init() {
    // Use the standard Base64 decoder to match your key format
    byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
    this.secretKey = Keys.hmacShaKeyFor(keyBytes);
  }

  // JWT token generation
  public String generateToken(User user) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION_TIME);

    return Jwts.builder()
        .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
        .setIssuer("eMusic")
        .setIssuedAt(now)
        .setExpiration(expiryDate)
//        .setSubject(user.getEmail())
        .claim("user_id", user.getId())
        .signWith(secretKey)
        .compact();
  }
}