package cloud.emusic.emotionmusicapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // API 문서의 기본 정보를 설정합니다.
        Info info = new Info()
                .title("Emotion Music API") // API 제목
                .version("v1.0.0") // API 버전
                .description("감정 분석 기반 음악 추천 서비스의 API 문서입니다."); // API 설명

        // JWT 인증을 위한 Security Scheme 설정 (인증이 필요할 경우)
        String jwtSchemeName = "JWT Authentication";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)   // HTTP 인증 방식
                        .scheme("bearer")                 // Bearer 인증
                        .bearerFormat("JWT")              // JWT 포맷
                        .in(SecurityScheme.In.HEADER));


        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}