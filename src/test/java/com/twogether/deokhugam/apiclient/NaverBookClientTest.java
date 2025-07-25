package com.twogether.deokhugam.apiclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twogether.deokhugam.apiclient.dto.NaverBookItem;
import com.twogether.deokhugam.apiclient.dto.NaverBookSearchResponse;
import com.twogether.deokhugam.book.dto.NaverBookDto;
import com.twogether.deokhugam.book.exception.NaverBookException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NaverBookClientTest {

    @InjectMocks // 테스트 대상인 NaverBookClientImpl에 Mock 객체 주입
    private NaverBookClientImpl naverBookClient;

    @Mock // RestTemplateBuilder를 Mock 객체로 생성
    private RestTemplateBuilder restTemplateBuilder;

    @Mock // RestTemplate을 Mock 객체로 생성
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper = new ObjectMapper(); // JSON 처리를 위한 ObjectMapper

    @BeforeEach // 각 테스트 메서드 실행 전 초기화
    void setUp() {
        // restTemplateBuilder.build() 호출 시 mock restTemplate 반환하도록 설정
        given(restTemplateBuilder.build()).willReturn(restTemplate);
        // NaverBookClientImpl의 생성자를 수동으로 호출하여 의존성 주입
        naverBookClient = new NaverBookClientImpl(restTemplateBuilder);

        // @Value로 주입되는 필드들을 ReflectionTestUtils를 사용하여 설정
        ReflectionTestUtils.setField(naverBookClient, "clientId", "testClientId");
        ReflectionTestUtils.setField(naverBookClient, "clientSecret", "testClientSecret");
        ReflectionTestUtils.setField(naverBookClient, "ocrSecret", "testOcrSecret");
        ReflectionTestUtils.setField(naverBookClient, "ocrUrl", "http://test-ocr-url.com");
    }

    @Test
    @DisplayName("ISBN으로 도서 정보 조회 성공 테스트")
    void fetchInfoByIsbnSuccess() {
        // given: 네이버 책 API 응답 Mock 설정
        String isbn = "978896077343";//테스트용 ISBN
        NaverBookItem item = new NaverBookItem(
                "테스트 도서", "테스트 저자", "테스트 설명", "테스트 출판사", "20230101", isbn, "http://test-image.com/book.jpg"
        );
        NaverBookSearchResponse mockResponse = new NaverBookSearchResponse(List.of(item));

        // restTemplate.exchange 호출 시 mockResponse 반환하도록 설정
        given(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(NaverBookSearchResponse.class)
        )).willReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // 이미지 다운로드 Mock 설정
        given(restTemplate.getForEntity(anyString(), eq(byte[].class)))
                .willReturn(new ResponseEntity<>("image_data".getBytes(), HttpStatus.OK));

        // when: ISBN으로 도서 정보 조회
        NaverBookDto result = naverBookClient.fetchInfoByIsbn(isbn);

        // then: 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("테스트 도서");
        assertThat(result.isbn()).isEqualTo(isbn);
        assertThat(result.publishedDate()).isEqualTo(LocalDate.of(2023, 1, 1));
        assertThat(result.thumbnailImage()).isEqualTo(java.util.Base64.getEncoder().encodeToString("image_data".getBytes()));
    }

    @Test
    @DisplayName("ISBN으로 도서 정보 조회 실패 테스트 - 유효하지 않은 ISBN")
    void fetchInfoByIsbnFail_InvalidIsbn() {
        // given: 유효하지 않은 ISBN
        String invalidIsbn = null;

        // when & then: NaverBookException 발생 확인
        NaverBookException exception = assertThrows(NaverBookException.class,
                () -> naverBookClient.fetchInfoByIsbn(invalidIsbn));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_ISBN);
    }

    @Test
    @DisplayName("ISBN으로 도서 정보 조회 실패 테스트 - 네이버 API 연결 실패")
    void fetchInfoByIsbnFail_ConnectionFailed() {
        // given: restTemplate.exchange 호출 시 RestClientException 발생
        given(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(NaverBookSearchResponse.class)
        )).willThrow(new RestClientException("Connection failed"));

        // when & then: NaverBookException 발생 확인
        NaverBookException exception = assertThrows(NaverBookException.class,
                () -> naverBookClient.fetchInfoByIsbn("1234567890"));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NAVER_API_CONNECTION_FAILED);
    }

    @Test
    @DisplayName("ISBN으로 도서 정보 조회 실패 테스트 - 도서 없음")
    void fetchInfoByIsbnFail_NoBookFound() {
        // given: 네이버 책 API 응답에 아이템이 없음
        NaverBookSearchResponse mockResponse = new NaverBookSearchResponse(Collections.emptyList());
        given(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(NaverBookSearchResponse.class)
        )).willReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // when: ISBN으로 도서 정보 조회
        NaverBookDto result = naverBookClient.fetchInfoByIsbn("1234567890");

        // then: 결과가 null인지 확인
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("OCR로 ISBN 추출 성공 테스트")
    void extractIsbnFromImageSuccess() throws Exception {
        // given: OCR 이미지와 OCR API 응답 Mock 설정
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "image_data".getBytes());
        String ocrResponseJson = """
    {
      "images": [{
        "fields": [
          {"inferText":"ISBN"},
          {"inferText":"979"},
          {"inferText":"11976930"},
          {"inferText":"07"}
        ]
      }]
    }
    """;

        given(restTemplate.postForEntity(
                eq("http://test-ocr-url.com"),
                any(HttpEntity.class),
                eq(String.class)
        )).willReturn(new ResponseEntity<>(ocrResponseJson, HttpStatus.OK));

        // when: OCR로 ISBN 추출
        String extractedIsbn = naverBookClient.extractIsbnFromImage(image);

        // then: 추출된 ISBN 검증
        assertThat(extractedIsbn).isEqualTo("9791197693007");

        // HttpEntity의 body가 올바르게 구성되었는지 확인
        ArgumentCaptor<HttpEntity> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(anyString(), httpEntityCaptor.capture(), any(Class.class));
        HttpEntity capturedEntity = httpEntityCaptor.getValue();
        assertThat(capturedEntity.getHeaders().getContentType()).isEqualTo(MediaType.MULTIPART_FORM_DATA);
        assertThat(capturedEntity.getHeaders().getFirst("X-OCR-SECRET")).isEqualTo("testOcrSecret");
    }

    @Test
    @DisplayName("OCR로 ISBN 추출 실패 테스트 - OCR 서버 에러")
    void extractIsbnFromImageFail_OcrServerError() throws Exception {
        // given: OCR 이미지와 5xx 에러 응답 Mock 설정
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "image_data".getBytes());

        given(restTemplate.postForEntity(
                eq("http://test-ocr-url.com"),
                any(HttpEntity.class),
                eq(String.class)
        )).willReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        // when & then: NaverBookException 발생 확인
        NaverBookException exception = assertThrows(NaverBookException.class,
                () -> naverBookClient.extractIsbnFromImage(image));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NAVER_OCR_SERVER_ERROR);
    }

    @Test
    @DisplayName("OCR로 ISBN 추출 실패 테스트 - OCR 권한 없음 에러")
    void extractIsbnFromImageFail_OcrUnauthorized() throws Exception {
        // given: OCR 이미지와 4xx 에러 응답 Mock 설정
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "image_data".getBytes());

        given(restTemplate.postForEntity(
                eq("http://test-ocr-url.com"),
                any(HttpEntity.class),
                eq(String.class)
        )).willReturn(new ResponseEntity<>(HttpStatus.UNAUTHORIZED));

        // when & then: NaverBookException 발생 확인
        NaverBookException exception = assertThrows(NaverBookException.class,
                () -> naverBookClient.extractIsbnFromImage(image));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NAVER_API_UNAUTHORIZED);
    }

    @Test
    @DisplayName("OCR로 ISBN 추출 실패 테스트 - ISBN을 찾을 수 없음")
    void extractIsbnFromImageFail_IsbnNotFound() throws Exception {
        // given: OCR 이미지와 ISBN이 없는 OCR API 응답 Mock 설정
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "image_data".getBytes());
        String ocrResponseJson = "{\"images\":[{\"fields\":[{\"inferText\":\"NoIsbnHere\"}]}]}";

        given(restTemplate.postForEntity(
                eq("http://test-ocr-url.com"),
                any(HttpEntity.class),
                eq(String.class)
        )).willReturn(new ResponseEntity<>(ocrResponseJson, HttpStatus.OK));

        // when & then: NaverBookException 발생 확인
        NaverBookException exception = assertThrows(NaverBookException.class,
                () -> naverBookClient.extractIsbnFromImage(image));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NAVER_OCR_ISBN_NOT_FOUND);
    }
}