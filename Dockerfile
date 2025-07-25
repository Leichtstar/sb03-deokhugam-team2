# 빌드 스테이지
FROM amazoncorretto:17 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle Wrapper 파일 먼저 복사
COPY gradle ./gradle
COPY gradlew ./gradlew
RUN chmod +x gradlew

# Gradle 캐시를 위한 의존성 파일 복사
COPY build.gradle settings.gradle ./

# 의존성 다운로드
RUN ./gradlew dependencies

# 소스 코드 복사 및 빌드
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon


# 런타임 스테이지
FROM amazoncorretto:17-alpine3.21

# 작업 디렉토리 설정
RUN mkdir -p /app/logs && chmod 755 /app/logs
WORKDIR /app

# 프로젝트 정보를 ENV로 설정
ENV JVM_OPTS=""

# 빌드 스테이지에서 jar 파일만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 8080 포트 노출
EXPOSE 8080

# jar 파일 실행
ENTRYPOINT ["sh", "-c", "java ${JVM_OPTS} -jar app.jar"]