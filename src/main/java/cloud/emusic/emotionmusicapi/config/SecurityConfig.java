package cloud.emusic.emotionmusicapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. 클래스에 있던 @Value 필드들을 삭제합니다.
    // @Value("${swagger.user.username}")
    // private String swaggerUsername;
    // @Value("${swagger.user.password}")
    // private String swaggerPassword;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).authenticated()
                        .anyRequest().permitAll()
                )
                .httpBasic(withDefaults());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 2. 허용할 프론트엔드 출처(Origin)를 지정합니다.
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:8080", // 로컬 테스트 환경 (React 기본 포트)
                "https://emusic.cloud",     // 배포 환경
                "https://api.emusic.cloud" // API 서버 도메인
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