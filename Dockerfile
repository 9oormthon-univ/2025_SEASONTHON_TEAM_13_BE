# 1. 빌드 스테이지 (의존성 설치 및 빌드 담당)
FROM gradle:8.14.3-jdk21 AS build
WORKDIR /app

# 의존성 캐시를 위해 빌드 관련 파일 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 다운로드 (이 단계가 캐시되어 빌드 속도 향상)
RUN gradle dependencies --no-daemon --stacktrace --info

# 전체 소스 코드 복사
COPY src ./src

# 애플리케이션 빌드 (테스트는 제외)
RUN gradle build -x test --no-daemon

# 2. 런타임 스테이지 (실제 애플리케이션 실행 담당)
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일만 복사
COPY --from=build /app/build/libs/emotionmusicapi-0.0.1-SNAPSHOT.jar app.jar

# 8080 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]