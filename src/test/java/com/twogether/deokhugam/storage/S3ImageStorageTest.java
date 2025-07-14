package com.twogether.deokhugam.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

/**
 * S3ImageStorage 클래스 테스트
 * - 이미지 업로드 성공/실패 케이스 테스트
 * - 파일 유효성 검증 테스트
 * - Mock을 사용하여 실제 S3 연결 없이 테스트
 */
@ExtendWith(MockitoExtension.class)  // Mockito 테스트 환경 설정
class S3ImageStorageTest {

    @Mock  // Mock 객체 생성
    private S3Client s3Client;

    private S3ImageStorage s3ImageStorage;

    // 테스트용 상수들
    private static final String TEST_BUCKET_NAME = "test-bucket";
    private static final String TEST_FOLDER_PATH = "test/images/";

    @BeforeEach  // 각 테스트 실행 전에 호출
    void setUp() {
        s3ImageStorage = new S3ImageStorage(s3Client);  // Mock 객체 주입해서 서비스 생성

        // private 필드에 테스트 값 설정 (ReflectionTestUtils 사용)
        ReflectionTestUtils.setField(s3ImageStorage, "bucketName", TEST_BUCKET_NAME);
    }

    @Test
    void 이미지_업로드_성공_테스트() throws IOException {
        // Given: 테스트용 이미지 파일 생성
        byte[] imageContent = "test-image-content".getBytes();
        MultipartFile imageFile = new MockMultipartFile(
            "test-image",           // 파라미터 이름
            "test.jpg",            // 원본 파일명
            "image/jpeg",          // Content-Type
            imageContent           // 파일 내용
        );

        // S3Client mock 응답 설정
        PutObjectResponse mockResponse = PutObjectResponse.builder()
            .eTag("mock-etag-12345")  // 업로드 성공 시 반환되는 ETag
            .build();

        // S3Client.putObject() 메서드 호출 시 mockResponse 반환하도록 설정
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(mockResponse);

        // When: 실제 업로드 메서드 실행
        String result = s3ImageStorage.uploadImage(imageFile, TEST_FOLDER_PATH);

        // Then: 결과 검증
        assertThat(result).isNotNull();
        assertThat(result).startsWith("https://" + TEST_BUCKET_NAME + ".s3.ap-northeast-2.amazonaws.com/");
        assertThat(result).contains(TEST_FOLDER_PATH);
        assertThat(result).contains(".jpg");

        // S3Client.putObject()가 한 번 호출되었는지 확인
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void 이미지_업로드_실패_테스트() throws IOException {
        // Given: 테스트용 이미지 파일 생성
        MultipartFile imageFile = new MockMultipartFile(
            "test-image", "test.jpg", "image/jpeg", "test-content".getBytes()
        );

        // S3Client에서 예외 발생하도록 설정
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenThrow(new RuntimeException("S3 연결 실패"));

        // When & Then: 예외 발생 검증
        assertThatThrownBy(() -> s3ImageStorage.uploadImage(imageFile, TEST_FOLDER_PATH))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("이미지 업로드 중 오류가 발생했습니다.");
    }

    @Test
    void 빈_파일_업로드_시_예외_발생() {
        // Given: 빈 파일 생성
        MultipartFile emptyFile = new MockMultipartFile(
            "empty-file", "empty.jpg", "image/jpeg", new byte[0]
        );

        // When & Then: IllegalArgumentException 발생 확인
        assertThatThrownBy(() -> s3ImageStorage.uploadImage(emptyFile, TEST_FOLDER_PATH))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("업로드할 이미지 파일이 없습니다.");
    }

    @Test
    void 이미지가_아닌_파일_업로드_시_예외_발생() {
        // Given: 이미지가 아닌 파일 (텍스트 파일)
        MultipartFile textFile = new MockMultipartFile(
            "text-file", "test.txt", "text/plain", "text content".getBytes()
        );

        // When & Then: IllegalArgumentException 발생 확인
        assertThatThrownBy(() -> s3ImageStorage.uploadImage(textFile, TEST_FOLDER_PATH))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이미지 파일만 업로드 가능합니다. 현재 타입: text/plain");
    }

    @Test
    void 파일_크기_초과_시_예외_발생() {
        // Given: 5MB 초과하는 큰 파일 (6MB)
        byte[] largeContent = new byte[6 * 1024 * 1024];  // 6MB
        MultipartFile largeFile = new MockMultipartFile(
            "large-file", "large.jpg", "image/jpeg", largeContent
        );

        // When & Then: IllegalArgumentException 발생 확인
        assertThatThrownBy(() -> s3ImageStorage.uploadImage(largeFile, TEST_FOLDER_PATH))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("이미지 파일 크기는 5MB를 초과할 수 없습니다");
    }

    @Test
    void 지원하지_않는_이미지_형식_업로드_시_예외_발생() {
        // Given: 지원하지 않는 이미지 형식 (GIF)
        MultipartFile gifFile = new MockMultipartFile(
            "gif-file", "test.gif", "image/gif", "gif content".getBytes()
        );

        // When & Then: IllegalArgumentException 발생 확인
        assertThatThrownBy(() -> s3ImageStorage.uploadImage(gifFile, TEST_FOLDER_PATH))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("지원하지 않는 이미지 형식입니다. 지원 형식: JPEG, JPG, PNG, WebP");
    }

    @Test
    void 다양한_이미지_형식_업로드_성공_테스트() throws IOException {
        // Given: 지원하는 다양한 이미지 형식들
        String[] supportedTypes = {"image/jpeg", "image/jpg", "image/png", "image/webp"};
        String[] extensions = {"jpg", "jpg", "png", "webp"};

        // S3Client mock 응답 설정
        PutObjectResponse mockResponse = PutObjectResponse.builder()
            .eTag("mock-etag")
            .build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(mockResponse);

        // When & Then: 각 형식별로 업로드 성공 확인
        for (int i = 0; i < supportedTypes.length; i++) {
            MultipartFile imageFile = new MockMultipartFile(
                "test-image",
                "test." + extensions[i],
                supportedTypes[i],
                "test content".getBytes()
            );

            String result = s3ImageStorage.uploadImage(imageFile, TEST_FOLDER_PATH);

            assertThat(result).isNotNull();
            assertThat(result).contains("." + extensions[i]);
        }

        // 4번의 업로드가 모두 성공했는지 확인
        verify(s3Client, times(4)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void 파일명이_null인_경우_기본_확장자_사용() throws IOException {
        // Given: 파일명이 null인 파일
        MultipartFile fileWithNullName = new MockMultipartFile(
            "test-image", null, "image/jpeg", "test content".getBytes()
        );

        // S3Client mock 응답 설정
        PutObjectResponse mockResponse = PutObjectResponse.builder()
            .eTag("mock-etag")
            .build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(mockResponse);

        // When: 업로드 실행
        String result = s3ImageStorage.uploadImage(fileWithNullName, TEST_FOLDER_PATH);

        // Then: 기본 확장자(.jpg)가 사용되었는지 확인
        assertThat(result).isNotNull();
        assertThat(result).endsWith(".jpg");
    }

    @Test
    void 고유한_파일명_생성_확인() throws IOException {
        // Given: 같은 이름의 파일 2개
        MultipartFile file1 = new MockMultipartFile(
            "test1", "same-name.jpg", "image/jpeg", "content1".getBytes()
        );
        MultipartFile file2 = new MockMultipartFile(
            "test2", "same-name.jpg", "image/jpeg", "content2".getBytes()
        );

        // S3Client mock 응답 설정
        PutObjectResponse mockResponse = PutObjectResponse.builder()
            .eTag("mock-etag")
            .build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(mockResponse);

        // When: 두 파일 업로드
        String result1 = s3ImageStorage.uploadImage(file1, TEST_FOLDER_PATH);
        String result2 = s3ImageStorage.uploadImage(file2, TEST_FOLDER_PATH);

        // Then: 두 파일의 URL이 다른지 확인 (고유한 파일명 생성)
        assertThat(result1).isNotEqualTo(result2);
        assertThat(result1).contains(TEST_FOLDER_PATH);
        assertThat(result2).contains(TEST_FOLDER_PATH);
    }
}
