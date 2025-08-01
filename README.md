# 덕후감 (Deokhugam)
[![codecov](https://codecov.io/gh/sb03-deokhugam-team2/sb03-deokhugam-team2/branch/dev/graph/badge.svg)](https://codecov.io/gh/sb03-deokhugam-team2/sb03-deokhugam-team2)

책 리뷰 및 독서 관리 플랫폼입니다.
<br>
https://www.notion.so/ohgiraffers/2gether-Read-207649136c1180e8a801c3cb2d59b319

## 👥 팀원 구성
박인규 (https://github.com/Leichtstar)  
이승진 (https://github.com/noonsong0208)  
정윤지 (https://github.com/okodeee)   
조백선 (https://github.com/bs8841)    
황지인 (https://github.com/wangcoJiin)

## 📚 프로젝트 소개
- 책을 사랑하는 사람들이 리뷰와 감상을 나누며 소통할 수 있는 책 덕후 전용 커뮤니티 플랫폼을 개발하고 안정적으로 배포
- 프로젝트 기간: 2024.07.08 ~ 2024.07.30

## 🛠️ 기술 스택
- **Java** 17
- **Spring Boot** 3.5.3
- **Spring Data JPA** & **QueryDSL** 5.0.0
- **Spring Batch** (랭킹 및 알림 정리 배치 작업)
- **PostgreSQL**
- **AWS S3** (이미지 및 로그 스토리지)
- **Naver API** (도서 검색 및 OCR)
- **Gradle** 8.x
- **MapStruct** 1.6.3 (객체 매핑)
- **Lombok** 
- **SpringDoc OpenAPI** 2.8.4 (API 문서화)
- **Docker & Docker Compose**

## 💻 개발 환경

- **IDE**: IntelliJ IDEA
- **Build Tool**: Gradle 8.x
- **Database**: PostgreSQL 13+
- **Container**: Docker & Docker Compose
- **Version Control**: Git
- **API Documentation**: SpringDoc OpenAPI 3
- **Testing**: JUnit 5, Mockito, H2 (테스트용)
- **Code Coverage**: JaCoCo (최소 80% 커버리지)

## 📋 요구사항

- Java 17 이상
- PostgreSQL 13 이상
- Docker & Docker Compose

## 🎯 팀원별 구현 기능 상세

### 정윤지: 사용자 관리 (`/api/users`)
- 사용자 회원가입 API (`POST /api/users`)
- 사용자 로그인 API (`POST /api/users/login`)
- 특정 사용자 조회 API (`GET /api/users/{userId}`)
- 사용자 정보 수정 API (`PATCH /api/users/{userId}`)
- 사용자 논리 삭제 API (`DELETE /api/users/{userId}`)
- 사용자 물리 삭제 API (`DELETE /api/users/{userId}/hard`)

### 박인규: 도서 관리 (`/api/books`)
- 도서 등록 API (`POST /api/books`)
- 도서 목록 조회 API (`GET /api/books`)
- 특정 도서 조회 API (`GET /api/books/{bookId}`)
- 도서 정보 수정 API (`PATCH /api/books/{bookId}`)
- 도서 논리 삭제 API (`DELETE /api/books/{bookId}`)
- 도서 물리 삭제 API (`DELETE /api/books/{bookId}/hard`)
- ISBN으로 도서 정보 조회 API (`GET /api/books/info?isbn={isbn}`) - Naver API 연동
- 이미지에서 ISBN 추출 API (`POST /api/books/isbn/ocr`) - Naver OCR API 연동

### 황지인: 리뷰 관리 (`/api/reviews`)
- 리뷰 작성 API (`POST /api/reviews`)
![녹화_2025_08_01_00_00_39_219](https://github.com/user-attachments/assets/e75f3528-2d01-4c24-9231-b10ff29f3b01)
- 리뷰 목록 조회 API (`GET /api/reviews`), 특정 리뷰 조회 API (`GET /api/reviews/{reviewId}`)
![녹화_2025_08_01_00_09_28_668](https://github.com/user-attachments/assets/ab58f09b-bea0-4d87-b550-4eadac474d9a)

- 리뷰 수정 API (`PATCH /api/reviews/{reviewId}`), 리뷰 논리 삭제 API (`DELETE /api/reviews/{reviewId}`)
![녹화_2025_08_01_00_11_52_507](https://github.com/user-attachments/assets/2e8ee783-a130-4a03-ac8f-a1decab131c8)
- 리뷰 물리 삭제 API (`DELETE /api/reviews/{reviewId}/hard`)
- 리뷰 좋아요/취소 API (`POST /api/reviews/{reviewId}/like`)
![녹화_2025_08_01_00_16_22_203](https://github.com/user-attachments/assets/6dea0a63-5e6a-4d25-ae80-06bf3d87128b)


### 이승진: 댓글 관리 (`/api/comments`)
- 댓글 작성 API (`POST /api/comments`)
- 특정 리뷰의 댓글 목록 조회 API (`GET /api/comments?reviewId={reviewId}`)
- 특정 댓글 조회 API (`GET /api/comments/{commentId}`)
- 댓글 수정 API (`PATCH /api/comments/{commentId}`)
- 댓글 논리 삭제 API (`DELETE /api/comments/{commentId}`)
- 댓글 물리 삭제 API (`DELETE /api/comments/{commentId}/hard`)

### 조백선: 알림 관리 (`/api/notifications`)
- 배치를 사용하는 알림 생성
- 알림 목록 조회 API (`GET /api/notifications`)
- 알림 읽음 처리 API (`PATCH /api/notifications/{notificationId}`)
- 모든 알림 읽음 처리 API (`PATCH /api/notifications/read-all`)

### 조백선: 대시보드 관리 (배치)
- 인기 도서 랭킹 조회 API (`GET /api/books/popular`)
- 인기 리뷰 랭킹 조회 API (`GET /api/reviews/popular`)
- 파워 유저 랭킹 조회 API (`GET /api/users/power`)

**공통 헤더**: 대부분의 API에서 `Deokhugam-Request-User-ID` 헤더를 통한 사용자 인증 필요

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/com/twogether/deokhugam/
│   │   ├── DeokhugamApplication.java
│   │   ├── apiclient/          # 외부 API 클라이언트 (Naver API)
│   │   ├── book/               # 도서 관련 기능
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   ├── dto/
│   │   │   └── exception/
│   │   ├── comments/           # 댓글 관련 기능
│   │   ├── common/             # 공통 기능 (DTO, Exception, Util)
│   │   ├── config/             # 설정 클래스들
│   │   ├── dashboard/          # 대시보드 및 랭킹
│   │   ├── notification/       # 알림 시스템
│   │   │   ├── event/          # 이벤트 기반 알림
│   │   │   ├── listener/       # 이벤트 리스너
│   │   │   └── batch/          # 알림 정리 배치
│   │   ├── review/             # 리뷰 관련 기능
│   │   ├── storage/            # S3 스토리지 연동
│   │   └── user/               # 사용자 관련 기능
│   └── resources/
│       ├── application.yaml           # 공통 설정
│       ├── application-dev.yaml       # 개발 환경 설정
│       ├── application-prod.yaml      # 운영 환경 설정
│       ├── application-test.yaml      # 테스트 환경 설정
│       ├── SCHEMA.sql                 # 데이터베이스 스키마
│       ├── data.sql                   # 초기 데이터
│       └── logback-spring.xml         # 로깅 설정
└── test/
    ├── java/
    └── resources/
        ├── application-test.yaml
        └── schema-test.sql
```

## ✨ 기능

### 핵심 기능
- [x] **사용자 관리 시스템** - 회원가입, 로그인, 프로필 관리
- [x] **도서 관리** - 도서 등록, 수정, 삭제 (논리/물리 삭제 지원)
- [x] **외부 API 연동**
    - [x] Naver 도서 검색 API (ISBN 기반 도서 정보 조회)
    - [x] Naver OCR API (이미지에서 ISBN 추출)
- [x] **리뷰 시스템** - 리뷰 작성, 수정, 삭제, 좋아요 기능
- [x] **댓글 시스템** - 리뷰에 대한 댓글 작성 및 관리
- [x] **파일 업로드** - AWS S3를 통한 이미지 업로드 및 스토리지
- [x] **실시간 알림 시스템** - 이벤트 기반 알림 생성 및 관리

### 랭킹 및 대시보드 (Spring Batch)
- [x] **인기 도서 랭킹** - 5분마다 자동 갱신
- [x] **인기 리뷰 랭킹** - 5분마다 자동 갱신
- [x] **파워 유저 랭킹** - 5분마다 자동 갱신
- [x] **알림 정리 작업** - 15분마다 오래된 알림 자동 정리

### 기술적 특징
- [x] **커서 기반 페이지네이션** - 대용량 데이터 효율적 처리
- [x] **QueryDSL** - 타입 안전한 복잡한 쿼리 처리
- [x] **멀티 프로필 지원** - 개발(dev), 운영(prod), 테스트(test) 환경 분리
- [x] **이벤트 드리븐 아키텍처** - Spring Events를 통한 느슨한 결합
- [x] **API 문서화** - SpringDoc OpenAPI 3 (Swagger UI)
- [x] **컨테이너 지원** - Docker 및 Docker Compose 설정
- [x] **테스트 커버리지** - JaCoCo를 통한 코드 품질 관리 (목표: 80%)
- [x] **로깅 시스템** - Logback 기반 구조화된 로깅

## 📖 구현 홈페이지
http://twogether-read.site/

## 💌 프로젝트 회고록
https://www.notion.so/codeit/3-2396fd228e8d808786e0ffccc7842337?p=2396fd228e8d805e99fef9879be8b529&pm=s
