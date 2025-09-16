# 이음 (Emusic) 음악으로 연결되는 감정 소셜 서비스
> 감정을 태그로 표현하고, 해당 감정에 어울리는 음악을 공유하며, 공감을 받을 수 있는 경험 제공
<img width="1278" height="718" alt="이음 프로젝트" src="https://github.com/user-attachments/assets/e3ae565b-14d3-40dc-9a76-052f6bd09fdd" />

## 문제 정의

- 최근 5년간 우울증 환자의 수가 급증
    - OECD 통계에 따르면 한국 성인 우울증 유병률은 36.8%로, 10명 중 4명꼴로 우울증을 경험
    
- 현재 감정 기반 소통 서비스의 부재
    - 현대 사회에서는 정신 건강과 정서적 소통의 중요성 증가
    - 가볍게 일상과 감정상태를 공유할 수 있는 플랫폼의 부재
      
- 현대인에게 음악은 감정 회복과 공감의 주요 수단
    - 단순히 취미를 넘어 스트레스 해소, 감정 치유, 자기표현의 영역으로 자리 매김
    - 개인의 감정을 표현하거나 위로하기 위한 매개체로 작용할 수 있다고 판단
      
- 감정 공유와 음악 감상의 융합 가능성
    - 감정을 기반으로 음악을 기록-추천-공유
    - 타인과 정서적 교감을 나눌 수 있는 서비스를 설계


## 서비스 설명
<table>
  <tr>
    <td><img width="1271" height="712" alt="이음 프로젝트 서비스 1" src="https://github.com/user-attachments/assets/daf1b13c-db64-4a47-b3e5-e58bcebd6e4b" /></td>
    <td><img width="1271" height="715" alt="이음 프로젝트 서비스 2" src="https://github.com/user-attachments/assets/bff24a3f-b500-4fde-b244-d9e7575e93bb" /></td>
  </tr>
  <tr>
    <td><img width="1272" height="715" alt="이음 프로젝트 서비스 3" src="https://github.com/user-attachments/assets/1a8e528f-442b-4faa-be57-ec07c7696f47" /></td>
    <td><img width="1277" height="711" alt="이음 프로젝트 서비스 4" src="https://github.com/user-attachments/assets/f7cd4feb-c82f-4b21-bbf4-93be58cfaae3" /></td>
  </tr>
</table>

## 핵심 기능

### 회원가입 / 로그인 (부속)
- 소셜 로그인(Kakao) 간편 회원가입 및 로그인 기능을 제공

### 나의 감정 / 음악 선택
- 매일 진입화면 (사용자가 매일 들어올 때 감정 및 음악 선택)

- 기능 :
    - **감정 태그 선택 :** 미리 정의된 감정 태그 목록(예: #기쁨, #슬픔 등)에서 현재 감정을 선택합니다
    - **하루 태그 :** 나의 하루를 표현할 태그를 사용자가 입력합니다
    - **감정 기반 음악 추천:** 선택한 감정 태그를 기반으로 노래를 추천합니다
    - **음악 검색 및 등록 :** Spotify API 등을 연동하여 음악을 검색하고 선택합니다
      
- 목표 : 사용자 자신의 감정과 감정에 어울리는 음악을 함께 기록하고 표현하는 경험을 제공합니다.

### 감정별 음악 피드 (탐색 및 공감)
- 기능 :
    - **피드 :** 다른 사용자가 등록한 감정+음악 게시물을 감정 태그별로 탐색할 수 있습니다
    - **공감표현 :** 각 게시물에 ❤️(좋아요)와 댓글을 통해 감정에 대한 공감을 표현할 수 있습니다
    - **음악 재생 :** 피드에서 선택한 음악을 내장 플레이어로 재생할 수 있습니다
    
- 목표 : 감정을 음악으로 공유하고, 서로의 감정을 공감하고 연결되는 경험을 유도합니다

### 좋아요 기반 감정 차트 *(소셜 기반 확장 기능)*

- 기능 : 감정 태그별로 등록된 음악 중 ❤️(좋아요)를 많이 받은 곡을 집계하여, 감정별 인기 음악 차트를 제공합니다.
- 목표 :  단순 스트리밍 수가 아닌, 감정에 기반한 공감(❤️)으로 구성된 차트를 통해 사용자에게 새로운 음악 별견을 돕습니다.

### 감정 달력 *(개인 감정 회고 기능)*

- 기능 : 사용자가 입력한 감정 태그 및 음악을 달력 형식으로 시각화 하여, 일별 감정 및 음악 기록을 한눈에 볼 수 있도록 시각화 합니다.
- 목표 : 개인의 감정 흐름을 회고하고 정리할 수 있는 감정 자가관찰 도구를 제공합니다.

## 기술스택
### BackEnd
- **Java 21 & Spring Boot 3.3.0** : 최신 LTS 버전 기반으로 안정적이고 빠른 개발 가능, Spring 생태계의 보안/ORM 등 확장 기능 활용
- **Spring Security + JWT** : 세션 기반보다 효율적이며, 인증/인가를 분리해 확장성과 보안성을 강화
- **OAuth2** : 소셜 로그인을 활용한 간편 로그인 기능 제공
- **JPA (Hibernate)** : 객체지향적인 데이터 접근 방식 제공, 반복적인 SQL 작성 부담 감소
- **Swagger (Springdoc OpenAPI)** : REST API 문서를 자동 생성하여 팀원 간 협업 및 테스트에 용이

### Database
- **MySQL (RDS)** : 안정적이고 범용적인 관계형 DB, 게시글/사용자/태그 등 정형 데이터 관리에 적합

### Infra & DevOps
- **AWS EC2** : 백엔드 서버 배포 및 운영 환경 제공, 프리티어로 비용 효율적 운영 가능
- **GitHub Actions** : CI/CD 파이프라인을 통해 자동 빌드·배포, 신속한 배포 및 피드백 사이클 지원
- **Docker** : 개발 환경과 배포 환경 간 차이를 최소화, 일관된 서비스 실행 보장
- **Cloudflare** : 도메인 네임 관리, SSL 인증서 제공, CDN 및 **DDoS 방어**를 통한 안정적인 서비스 운영
- **Spring Boot Actuator + Prometheus + Grafana** : 애플리케이션 상태 및 메트릭 모니터링, 시각화 대시보드를 통한 성능 분석 및 장애 대응

### Collaboration
- **Figma** : UI/UX 프로토타이핑 및 디자인 공유
- **Notion, GitHub** : 협업 문서화, 일정 관리, 이슈 트래킹을 통한 효율적인 팀 협업
- **Discord** : 팀 간 실시간 커뮤니케이션(텍스트/음성/화상), 알림 및 빠른 의사소통 지원

## 아키텍쳐
<img width="1169" height="827" alt="이음 프로젝트 아키텍쳐" src="https://github.com/user-attachments/assets/9de39e62-d7cc-4817-a0d1-15a664e598a0" />

## 패키지 구조 
``` plaintext
└─src
    ├─main
    │  ├─java
    │  │  └─cloud
    │  │      └─emusic
    │  │          └─emotionmusicapi
    │  │              ├─config          # 전역 설정 관련 (보안, JWT 등)
    │  │              │  └─jwt            # JWT 토큰 발급/검증 로직
    │  │              ├─controller      # REST API 엔드포인트
    │  │              ├─domain          # 엔티티 클래스 (DB 매핑)
    │  │              │  ├─comment        # 댓글 관련 엔티티
    │  │              │  ├─post           # 게시글 관련 엔티티
    │  │              │  ├─song           # 곡(음악) 관련 엔티티
    │  │              │  ├─tag            # 감정/태그 관련 엔티티
    │  │              │  └─user           # 사용자 관련 엔티티
    │  │              ├─dto             # 요청/응답 DTO
    │  │              │  ├─request        # 요청 DTO 모음
    │  │              │  └─response       # 응답 DTO 모음
    │  │              │      ├─comment      # 댓글 응답 DTO
    │  │              │      ├─login        # 로그인/회원가입 응답 DTO
    │  │              │      ├─post         # 게시글 응답 DTO
    │  │              │      ├─song         # 곡 응답 DTO
    │  │              │      ├─tag          # 태그 응답 DTO
    │  │              │      └─user         # 사용자 응답 DTO
    │  │              ├─exception       # 예외 처리 관련
    │  │              │  └─dto            # 에러 응답 DTO
    │  │              ├─repository      # JPA Repository (DB 접근 계층)
    │  │              └─service         # 비즈니스 로직 계층
    │  └─resources                      # 리소스(설정/정적 파일/템플릿) 관리
    └─test/java/com/emotionmusic        # 테스트 코드

```
