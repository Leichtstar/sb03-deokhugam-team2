package com.twogether.deokhugam.storage;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Slf4j
@Component
public class S3LogStorage {
    private final S3Client s3Client;

    @Value("${AWS_S3_BUCKET}")
    private String bucketName;

    @Value("${AWS_S3_REGION}")
    private String region;

    @Value("${logging.file.path:./logs}")
    private String logPath;

    @Value("${logging.file.name:application}")
    private String logFileName;

    public S3LogStorage(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * 매일 새벽 2시에 어제 날짜의 로그 파일을 S3에 업로드
     * cron 표현식: 초(0) 분(0) 시(2) 일(*) 월(*) 요일(*)
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void uploadDailyLogToS3() {
        log.info("일일 로그 파일 S3 업로드 작업 시작");

        // 어제 날짜 계산
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dateString = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 업로드할 로그 파일 경로 생성
        String logFilePath = logPath + "/" + logFileName + "." + dateString + ".log";

        try {
            uploadLogFile(logFilePath, dateString);
        } catch (Exception e) {
            log.error("일일 로그 업로드 작업 실패: {}", e.getMessage(), e);
        }

        log.info("일일 로그 파일 S3 업로드 작업 완료");
    }

    /**
     * 실제 로그 파일을 S3에 업로드하는 핵심 메서드
     * @param logFilePath 업로드할 로그 파일의 전체 경로
     * @param dateString 날짜 문자열 (S3 경로 생성용)
     */
    private void uploadLogFile(String logFilePath, String dateString) {
        File logFile = new File(logFilePath);

        // 파일 존재 여부 확인
        if (!logFile.exists()) {
            log.warn("로그 파일이 존재하지 않습니다: {}", logFilePath);
            return;
        }

        // 파일 크기 확인 (0바이트 파일은 업로드하지 않음)
        if (logFile.length() == 0) {
            log.warn("빈 로그 파일입니다: {}", logFilePath);
            return;
        }

        try {
            // S3에 저장할 키(경로) 생성
            // 구조: logs/년도/월/파일명
            // 예시: logs/2025/07/application.2025-07-22.log
            String[] dateParts = dateString.split("-");
            String year = dateParts[0];
            String month = dateParts[1];

            String s3Key = String.format("logs/%s/%s/%s.%s.log",
                year, month, logFileName, dateString);

            // S3 업로드 요청 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType("text/plain")  // 로그 파일은 텍스트 타입
                .contentLength(logFile.length())  // 파일 크기 명시
                .build();

            log.info("로그 파일 S3 업로드 시작 - 로컬경로: {}, S3경로: {}, 크기: {}바이트",
                logFilePath, s3Key, logFile.length());

            // 파일을 S3에 업로드
            PutObjectResponse response = s3Client.putObject(
                putObjectRequest,
                RequestBody.fromFile(logFile)
            );

            log.info("로그 파일 S3 업로드 완료 - S3 Key: {}, ETag: {}", s3Key, response.eTag());

            // 업로드 성공 후 로컬 파일 삭제
            if (logFile.delete()) {
                log.info("업로드 완료 후 로컬 로그 파일 삭제: {}", logFilePath);
            } else {
                log.warn("로컬 로그 파일 삭제 실패: {}", logFilePath);
            }

        } catch (Exception e) {
            log.error("S3 업로드 실패 - 파일: {}, 오류: {}", logFilePath, e.getMessage(), e);
            throw new RuntimeException("로그 파일 S3 업로드 중 오류가 발생했습니다.", e);
        }
    }
}
