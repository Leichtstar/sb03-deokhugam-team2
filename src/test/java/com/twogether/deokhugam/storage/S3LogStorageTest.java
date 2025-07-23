package com.twogether.deokhugam.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

/**
 * S3LogStorage 클래스의 단위 테스트
 * Mockito를 사용해서 S3Client를 모킹하여 실제 S3 없이 테스트
 */
@ExtendWith(MockitoExtension.class)  // Mockito 확장 활성화
@ActiveProfiles("test")  // 테스트 프로파일 활성화
class S3LogStorageTest {

    @Mock
    private S3Client s3Client;  // S3Client를 모킹

    @InjectMocks
    private S3LogStorage s3LogStorage;  // 테스트 대상 객체

    @TempDir
    Path tempDir;  // 임시 디렉토리 (로그 파일 생성용)

    private static final String TEST_BUCKET = "test-bucket";
    private static final String TEST_REGION = "ap-northeast-2";
    private static final String TEST_LOG_FILE_NAME = "application";

    @BeforeEach  // 각 테스트 실행 전에 호출
    void setUp() {
        // @Value로 주입되는 필드들을 수동으로 설정
        ReflectionTestUtils.setField(s3LogStorage, "bucketName", TEST_BUCKET);
        ReflectionTestUtils.setField(s3LogStorage, "region", TEST_REGION);
        ReflectionTestUtils.setField(s3LogStorage, "logPath", tempDir.toString());
        ReflectionTestUtils.setField(s3LogStorage, "logFileName", TEST_LOG_FILE_NAME);
    }

    @Test
    @DisplayName("로그 파일이 존재하지 않을 때 업로드가 스킵되는지 테스트")
    void uploadDailyLogToS3_파일없음_스킵() {
        // Given: 로그 파일을 생성하지 않음 (존재하지 않는 상태)

        // When: 업로드 메서드 실행
        assertDoesNotThrow(() -> s3LogStorage.uploadDailyLogToS3());

        // Then: S3Client가 호출되지 않았는지 확인
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("빈 로그 파일(0바이트)일 때 업로드가 스킵되는지 테스트")
    void uploadDailyLogToS3_빈파일_스킵() throws IOException {
        // Given: 빈 로그 파일 생성
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dateString = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String logFileName = TEST_LOG_FILE_NAME + "." + dateString + ".log";

        Path logFile = tempDir.resolve(logFileName);
        Files.createFile(logFile);  // 빈 파일 생성 (0바이트)

        // When: 업로드 메서드 실행
        assertDoesNotThrow(() -> s3LogStorage.uploadDailyLogToS3());

        // Then: S3Client가 호출되지 않았는지 확인
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // 빈 파일은 삭제되지 않고 그대로 남아있어야 함
        assertTrue(Files.exists(logFile), "빈 파일은 업로드되지 않아 삭제되지 않아야 함");
    }

    @Test
    @DisplayName("S3 업로드 실패 시 예외가 발생하는지 테스트")
    void uploadDailyLogToS3_S3업로드실패_예외발생() throws IOException {
        // Given: 테스트 로그 파일 생성
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dateString = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String logFileName = TEST_LOG_FILE_NAME + "." + dateString + ".log";

        Path logFile = tempDir.resolve(logFileName);
        String logContent = "테스트 로그 내용";
        Files.write(logFile, logContent.getBytes());

        // S3Client 모킹 - putObject 호출 시 예외 발생
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenThrow(new RuntimeException("S3 연결 실패"));

        // When & Then: 예외가 발생하지만 메서드는 정상적으로 완료되어야 함
        // (uploadDailyLogToS3 메서드 내부에서 예외를 catch하고 로그로 남김)
        assertDoesNotThrow(() -> s3LogStorage.uploadDailyLogToS3());

        // S3Client 호출 확인
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // 업로드 실패 시 로컬 파일은 삭제되지 않아야 함
        assertTrue(Files.exists(logFile), "업로드 실패 시 로컬 파일이 보존되어야 함");
    }

    @Test
    @DisplayName("대용량 로그 파일 업로드 테스트")
    void 대용량_로그파일_업로드() throws IOException {
        // Given: 큰 로그 파일 생성 (1MB)
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dateString = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String logFileName = TEST_LOG_FILE_NAME + "." + dateString + ".log";

        Path logFile = tempDir.resolve(logFileName);

        // 1MB 크기의 로그 내용 생성
        StringBuilder largeContent = new StringBuilder();
        String logLine = "2025-07-23 10:00:00.000 [main] INFO - 대용량 테스트 로그 메시지입니다.\n";
        int targetSize = 1024 * 1024;  // 1MB
        while (largeContent.length() < targetSize) {
            largeContent.append(logLine);
        }

        Files.write(logFile, largeContent.toString().getBytes());

        // S3Client 모킹
        PutObjectResponse mockResponse = PutObjectResponse.builder()
            .eTag("large-file-etag")
            .build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(mockResponse);

        // When: 업로드 실행
        assertDoesNotThrow(() -> s3LogStorage.uploadDailyLogToS3());

        // Then: 파일 크기가 올바르게 전송되었는지 확인
        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest request = requestCaptor.getValue();
        assertTrue(request.contentLength() >= targetSize,
            "대용량 파일의 크기가 올바르게 설정되어야 함");
    }
}