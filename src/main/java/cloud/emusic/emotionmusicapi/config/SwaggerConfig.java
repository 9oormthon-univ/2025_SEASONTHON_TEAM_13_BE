package cloud.emusic.emotionmusicapi.config;

import cloud.emusic.emotionmusicapi.exception.ApiExceptions;
import cloud.emusic.emotionmusicapi.exception.ErrorResponse;
import cloud.emusic.emotionmusicapi.exception.dto.ErrorCode;
import cloud.emusic.emotionmusicapi.exception.dto.ExampleHolder;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Bean
    public OperationCustomizer customize() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            // 컨트롤러 메서드에서 @ApiExceptions 어노테이션 가져오기
            ApiExceptions apiExceptions = handlerMethod.getMethodAnnotation(ApiExceptions.class);

            // 에러 코드가 지정되어 있다면 Swagger 응답 예시에 추가
            if (apiExceptions != null) {
                generateErrorCodeResponseExample(operation, apiExceptions.values());
            }

            return operation;
        };
    }

    // ErrorCode 배열을 받아 Swagger ApiResponses에 예시 추가
    private void generateErrorCodeResponseExample(Operation operation, ErrorCode[] errorCodes) {
        ApiResponses responses = operation.getResponses();

        // ErrorCode 별 ExampleHolder 생성 후 상태코드별 그룹핑
        Map<Integer, List<ExampleHolder>> statusWithExampleHolders = Arrays.stream(errorCodes)
                .map(errorCode -> ExampleHolder.builder()
                        .holder(getSwaggerExample(errorCode))
                        .code(errorCode.getStatus().value())
                        .name(errorCode.name())
                        .build()
                )
                .collect(Collectors.groupingBy(ExampleHolder::getCode));

        // 실제 Swagger 응답에 예시 등록
        addExamplesToResponses(responses, statusWithExampleHolders);
    }

    // 단일 ErrorCode → ErrorResponse → Swagger Example 변환
    private Example getSwaggerExample(ErrorCode errorCode) {
        ErrorResponse errorResponse = ErrorResponse.of(errorCode);

        Example example = new Example();
        example.setValue(errorResponse);
        example.setSummary(errorCode.name());
        return example;
    }

    // grouping된 상태코드별 예시를 Swagger ApiResponses에 등록
    private void addExamplesToResponses(ApiResponses responses,
                                        Map<Integer, List<ExampleHolder>> statusWithExampleHolders) {
        statusWithExampleHolders.forEach((statusCode, exampleHolders) -> {
            ApiResponse apiResponse = new ApiResponse().description("에러 응답");

            Content content = new Content();
            MediaType mediaType = new MediaType();

            exampleHolders.forEach(holder ->
                    mediaType.addExamples(holder.getName(), holder.getHolder())
            );

            content.addMediaType("application/json", mediaType);
            apiResponse.setContent(content);

            responses.addApiResponse(String.valueOf(statusCode), apiResponse);
        });
    }
}