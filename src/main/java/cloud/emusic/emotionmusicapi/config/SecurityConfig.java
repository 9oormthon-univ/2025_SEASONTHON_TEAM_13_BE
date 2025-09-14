package cloud.emusic.emotionmusicapi.config;

import cloud.emusic.emotionmusicapi.config.jwt.JwtAuthenticationFilter;
import cloud.emusic.emotionmusicapi.config.jwt.JwtTokenProvider;
import cloud.emusic.emotionmusicapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter() {
    return new JwtAuthenticationFilter(jwtTokenProvider,userRepository);
  }

  @Bean
  @Order(1)
  public SecurityFilterChain swaggerSecurity(HttpSecurity http) throws Exception {
    http.cors(withDefaults())
        .securityMatcher("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
                // Swagger 관련 URL은 SWAGGER 역할이 있는 사용자만 접근 가능
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).hasRole("SWAGGER")
            // 로그인(JWT 인증)된 사용자만 접근 가능
        )
        .httpBasic(withDefaults());
    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
    http.cors(withDefaults())
        .securityMatcher("/**")
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/login/url","/login/authenticate","/posts/emotion-tags","/tags/**","/songs/**").permitAll()
            .requestMatchers("/posts/**","/users/me").authenticated()
            .anyRequest().permitAll()
        )
        .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // 2. 허용할 프론트엔드 출처(Origin)를 지정합니다.
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:8080", // 로컬 테스트 환경 (React 기본 포트)
        "https://emusic.cloud",     // 배포 환경
        "https://www.emusic.cloud",
        "https://api.emusic.cloud", // API 서버 도메인
        "http://localhost:5173",
        "http://127.0.0.1:5173"
    ));

    // 3. 허용할 HTTP 메서드를 지정합니다. (*는 모든 메서드를 의미)
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

    // 4. 허용할 HTTP 헤더를 지정합니다.
    configuration.setAllowedHeaders(Arrays.asList("*"));

    // 5. 쿠키 등 자격 증명 정보를 허용합니다.
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 위 설정을 적용
    return source;
  }

  @Bean
  public UserDetailsService userDetailsService(
      @Value("${SWAGGER_USER_USERNAME}") String username,
      @Value("${SWAGGER_USER_PASSWORD}") String password) {

    // 아래 디버깅 코드 추가
    System.out.println("===== Creating Swagger User ======");
    System.out.println("Username from @Value: '" + username + "'");
    System.out.println("Password from @Value: '" + password + "'");
    System.out.println("================================");

    UserDetails swaggerUser = User.builder()
        .username(username)
        .password("{noop}" + password)
        .roles("SWAGGER")
        .build();

    return new InMemoryUserDetailsManager(swaggerUser);
  }
}
