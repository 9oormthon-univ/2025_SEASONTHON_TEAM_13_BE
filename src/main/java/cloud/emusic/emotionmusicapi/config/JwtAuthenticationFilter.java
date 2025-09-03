package cloud.emusic.emotionmusicapi.config;

import cloud.emusic.emotionmusicapi.domain.User;
import cloud.emusic.emotionmusicapi.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final static String HEADER_STRING = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";

    // 요청이 들어올 때마다 실행되는 메서드
    // JWT가 유효하면 인증 객체(SecurityContext)를 설정한다
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 요청 헤더에서 JWT 추출
        String token = resolveToken(request);

        // 2. 토큰이 존재하고 유효하면
        if (token != null && jwtTokenProvider.validateToken(token)) {

            // 3. 토큰에서 사용자 ID 추출
            String userId = jwtTokenProvider.getUserId(token);
            User user = userRepository.findById(Long.parseLong(userId)).orElse(null);

            if (user != null) {
                // 4. 인증 객체 생성 (권한 포함)
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
                        List.of(new SimpleGrantedAuthority(user.getRole().name())));

                // 5. SecurityContext에 인증 정보 등록
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        // 6. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // 1. Authorization 헤더 우선 체크
        String bearerToken = request.getHeader(HEADER_STRING);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(7);
        }

        // 2. access_token 쿠키에서 추출
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}