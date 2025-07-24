package com.twogether.deokhugam.book.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.twogether.deokhugam.book.dto.BookDto;
import com.twogether.deokhugam.book.dto.request.BookCreateRequest;
import com.twogether.deokhugam.book.dto.request.BookUpdateRequest;
import com.twogether.deokhugam.book.dto.response.BookPageResponse;
import com.twogether.deokhugam.book.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class BookControllerTest {

    @InjectMocks // 테스트 대상인 BookController에 Mock 객체 주입
    private BookController bookController;

    @Mock // BookController가 의존하는 BookService를 Mock 객체로 생성
    private BookService bookService;

    private MockMvc mockMvc; // MockMvc 객체, 컨트롤러 테스트를 위한 HTTP 요청 수행

    private ObjectMapper objectMapper; // JSON 직렬화/역직렬화를 위한 ObjectMapper

    @BeforeEach // 각 테스트 메서드 실행 전 초기화
    public void setUp() {
        // MockMvc 설정: BookController를 대상으로 HTTP 요청을 시뮬레이션
        mockMvc = MockMvcBuilders.standaloneSetup(bookController).build();
        objectMapper = new ObjectMapper(); // ObjectMapper 인스턴스 생성
        objectMapper.registerModule(new JavaTimeModule()); // LocalDate 직렬화 모듈 추가
    }

    @Test
    @DisplayName("도서 등록 성공 테스트 - 썸네일 이미지 포함")
    void registerBookWithThumbnailSuccess() throws Exception {
        // given: BookCreateRequest 객체와 MockMultipartFile 생성
        BookCreateRequest request = new BookCreateRequest(
                "테스트 도서",
                "테스트 저자",
                "테스트 설명",
                "테스트 출판사",
                LocalDate.now(),
                "1234567890"
        );

        MockMultipartFile bookData = new MockMultipartFile(
                "bookData", // @RequestPart("bookData")와 일치해야 함
                "", // 파일 이름은 비워둠 (JSON 데이터이므로)
                MediaType.APPLICATION_JSON_VALUE, // Content-Type
                objectMapper.writeValueAsBytes(request) // 요청 객체를 JSON 바이트로 변환
        );

        MockMultipartFile thumbnailImage = new MockMultipartFile(
                "thumbnailImage", // @RequestPart("thumbnailImage")와 일치해야 함
                "thumbnail.jpg", // 파일 이름
                MediaType.IMAGE_JPEG_VALUE, // Content-Type
                "thumbnail content".getBytes() // 이미지 내용
        );

        // BookService.registerBook 메서드가 호출될 때 반환할 BookDto 객체 생성
        BookDto expectedBookDto = new BookDto(
                UUID.randomUUID(),
                "테스트 도서",
                "테스트 저자",
                "테스트 설명",
                "테스트 출판사",
                LocalDate.of(2023, 1, 1),
                "1234567890",
                "http://example.com/thumbnail.jpg",
                0,
                0.0,
                Instant.now(),
                Instant.now()
        );

        // Mocking: bookService.registerBook(request, thumbnailImage) 호출 시 expectedBookDto 반환하도록 설정
        given(bookService.registerBook(any(BookCreateRequest.class), any(MockMultipartFile.class)))
                .willReturn(expectedBookDto);

        // when & then: HTTP POST 요청 수행 및 결과 검증
        mockMvc.perform(multipart("/api/books") // multipart 요청
                        .file(bookData) // bookData 파트 추가
                        .file(thumbnailImage) // thumbnailImage 파트 추가
                        .contentType(MediaType.MULTIPART_FORM_DATA)) // Content-Type 설정
                .andExpect(status().isCreated()) // HTTP 상태 코드가 201 Created인지 확인
                .andExpect(jsonPath("$.title").value(expectedBookDto.title())) // JSON 응답의 title 필드 검증
                .andExpect(jsonPath("$.isbn").value(expectedBookDto.isbn())); // JSON 응답의 isbn 필드 검증
    }

    @Test
    @DisplayName("도서 등록 성공 테스트 - 썸네일 이미지 없음")
    void registerBookWithoutThumbnailSuccess() throws Exception {
        // given: BookCreateRequest 객체 생성 (썸네일 이미지 없음)
        BookCreateRequest request = new BookCreateRequest(
                "테스트 도서 (이미지 없음)",
                "테스트 저자",
                "테스트 설명",
                "테스트 출판사",
                LocalDate.now(),
                "0987654321"
        );

        MockMultipartFile bookData = new MockMultipartFile(
                "bookData",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        // BookService.registerBook 메서드가 호출될 때 반환할 BookDto 객체 생성
        BookDto expectedBookDto = new BookDto(
                UUID.randomUUID(),
                "테스트 도서 (이미지 없음)",
                "테스트 저자",
                "테스트 설명",
                "테스트 출판사",
                LocalDate.of(2023, 1, 1),
                "0987654321",
                null,
                0,
                0.0,
                Instant.now(),
                Instant.now()
        );

        // Mocking: bookService.registerBook(request) 호출 시 expectedBookDto 반환하도록 설정
        given(bookService.registerBook(any(BookCreateRequest.class)))
                .willReturn(expectedBookDto);

        // when & then: HTTP POST 요청 수행 및 결과 검증
        mockMvc.perform(multipart("/api/books")
                        .file(bookData)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(expectedBookDto.title()))
                .andExpect(jsonPath("$.isbn").value(expectedBookDto.isbn()));
    }

    @Test
    @DisplayName("도서 수정 성공 테스트 - 썸네일 이미지 포함")
    void updateBookWithThumbnailSuccess() throws Exception {
        // given: BookUpdateRequest 객체와 MockMultipartFile 생성
        UUID bookId = UUID.randomUUID();
        BookUpdateRequest request = new BookUpdateRequest(
                "수정된 도서 제목",
                "테스트 저자",
                "수정된 설명",
                "테스트 출판사",
                LocalDate.now()
        );

        MockMultipartFile bookData = new MockMultipartFile(
                "bookData",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile thumbnailImage = new MockMultipartFile(
                "thumbnailImage",
                "updated_thumbnail.png",
                MediaType.IMAGE_PNG_VALUE,
                "updated thumbnail content".getBytes()
        );

        // BookService.updateBook 메서드가 호출될 때 반환할 BookDto 객체 생성
        BookDto expectedBookDto = new BookDto(
                bookId,
                "수정된 도서 제목",
                "테스트 저자",
                "수정된 설명",
                "테스트 출판사",
                LocalDate.of(2023, 1, 1),
                "1234567890",
                "http://example.com/updated_thumbnail.png",
                0,
                0.0,
                Instant.now(),
                Instant.now()
        );

        // Mocking: bookService.updateBook(bookId, request, thumbnailImage) 호출 시 expectedBookDto 반환하도록 설정
        given(bookService.updateBook(eq(bookId), any(BookUpdateRequest.class), any(MockMultipartFile.class)))
                .willReturn(expectedBookDto);

        // when & then: HTTP PATCH 요청 수행 및 결과 검증
        mockMvc.perform(multipart("/api/books/{bookId}", bookId)
                        .file(bookData)
                        .file(thumbnailImage)
                        .with(req -> { // PATCH 메서드를 사용하기 위해 요청 메서드 오버라이드
                            req.setMethod("PATCH");
                            return req;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk()) // HTTP 상태 코드가 200 OK인지 확인
                .andExpect(jsonPath("$.title").value(expectedBookDto.title()))
                .andExpect(jsonPath("$.description").value(expectedBookDto.description()))
                .andExpect(jsonPath("$.thumbnailUrl").value(expectedBookDto.thumbnailUrl()));
    }

    @Test
    @DisplayName("도서 수정 성공 테스트 - 썸네일 이미지 없음")
    void updateBookWithoutThumbnailSuccess() throws Exception {
        // given: BookUpdateRequest 객체 생성 (썸네일 이미지 없음)
        UUID bookId = UUID.randomUUID();
        BookUpdateRequest request = new BookUpdateRequest(
                "수정된 도서 제목 (이미지 없음)",
                "테스트 저자",
                "수정된 설명 (이미지 없음)",
                "테스트 출판사",
                LocalDate.now()
        );

        MockMultipartFile bookData = new MockMultipartFile(
                "bookData",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        // BookService.updateBook 메서드가 호출될 때 반환할 BookDto 객체 생성
        BookDto expectedBookDto = new BookDto(
                bookId,
                "수정된 도서 제목 (이미지 없음)",
                "테스트 저자",
                "수정된 설명 (이미지 없음)",
                "테스트 출판사",
                LocalDate.of(2023, 1, 1),
                "1234567890",
                "http://example.com/original_thumbnail.jpg",
                0,
                0.0,
                Instant.now(),
                Instant.now()
        );

        // Mocking: bookService.updateBook(bookId, request, null) 호출 시 expectedBookDto 반환하도록 설정
        given(bookService.updateBook(eq(bookId), any(BookUpdateRequest.class), eq(null)))
                .willReturn(expectedBookDto);

        // when & then: HTTP PATCH 요청 수행 및 결과 검증
        mockMvc.perform(multipart("/api/books/{bookId}", bookId)
                        .file(bookData)
                        .with(req -> {
                            req.setMethod("PATCH");
                            return req;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(expectedBookDto.title()))
                .andExpect(jsonPath("$.description").value(expectedBookDto.description()))
                .andExpect(jsonPath("$.thumbnailUrl").value(expectedBookDto.thumbnailUrl()));
    }

    @Test
    @DisplayName("모든 도서 조회 성공 테스트")
    void getAllBooksSuccess() throws Exception {
        // given: BookDto 리스트와 BookPageResponse 객체 생성
        List<BookDto> bookList = Arrays.asList(
                new BookDto(UUID.randomUUID(), "도서1", "", "", "", LocalDate.now(), "111", "", 0, 0.0, Instant.now(), Instant.now()),
                new BookDto(UUID.randomUUID(), "도서2", "", "", "", LocalDate.now(), "222", "", 0, 0.0, Instant.now(), Instant.now())
        );
        BookPageResponse<BookDto> expectedResponse = new BookPageResponse<>(
            bookList,              // content
            "nextCursor",          // nextCursor
            Instant.now(),         // nextAfter
            bookList.size(),       // size
            100L,                  // totalElements
            true                   // hasNext
        );
        // Mocking: bookService.getAllSorted 호출 시 expectedResponse 반환하도록 설정
        given(bookService.getAllSorted(anyString(), anyString(), anyString(), anyString(), any(Instant.class), anyInt()))
                .willReturn(expectedResponse);

        // when & then: HTTP GET 요청 수행 및 결과 검증
        mockMvc.perform(get("/api/books")
                        .param("keyword", "테스트")
                        .param("orderBy", "title")
                        .param("direction", "ASC")
                        .param("cursor", "someCursor")
                        .param("after", Instant.now().toString())
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // HTTP 상태 코드가 200 OK인지 확인
                .andExpect(jsonPath("$.content.length()").value(bookList.size())) // 응답 리스트 크기 검증
                .andExpect(jsonPath("$.content[0].title").value("도서1")) // 첫 번째 도서의 title 검증
                .andExpect(jsonPath("$.nextCursor").value("nextCursor")); // nextCursor 검증
    }

    @Test
    @DisplayName("단일 도서 조회 성공 테스트")
    void getBookSuccess() throws Exception {
        // given: 조회할 BookId와 반환할 BookDto 객체 생성
        UUID bookId = UUID.randomUUID();
        BookDto expectedBookDto = new BookDto(
                bookId,
                "조회된 도서",
                "",
                "",
                "",
                LocalDate.now(),
                "9999999999",
                "",
                0,
                0.0,
                Instant.now(),
                Instant.now()
        );

        // Mocking: bookService.getBookbyId 호출 시 expectedBookDto 반환하도록 설정
        given(bookService.getBookbyId(eq(bookId))).willReturn(expectedBookDto);

        // when & then: HTTP GET 요청 수행 및 결과 검증
        mockMvc.perform(get("/api/books/{bookId}", bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // HTTP 상태 코드가 200 OK인지 확인
                .andExpect(jsonPath("$.id").value(bookId.toString())) // JSON 응답의 bookId 필드 검증
                .andExpect(jsonPath("$.title").value(expectedBookDto.title())); // JSON 응답의 title 필드 검증
    }

    @Test
    @DisplayName("도서 논리 삭제 성공 테스트")
    void deleteBookSuccess() throws Exception {
        // given: 삭제할 BookId
        UUID bookId = UUID.randomUUID();

        // Mocking: bookService.deleteBook 호출 시 아무것도 반환하지 않도록 설정
        doNothing().when(bookService).deleteBook(eq(bookId));

        // when & then: HTTP DELETE 요청 수행 및 결과 검증
        mockMvc.perform(delete("/api/books/{bookId}", bookId))
                .andExpect(status().isNoContent()); // HTTP 상태 코드가 204 No Content인지 확인
    }

    @Test
    @DisplayName("도서 물리 삭제 성공 테스트")
    void deleteBookHardSuccess() throws Exception {
        // given: 물리 삭제할 BookId
        UUID bookId = UUID.randomUUID();

        // Mocking: bookService.deleteBookHard 호출 시 아무것도 반환하지 않도록 설정
        doNothing().when(bookService).deleteBookHard(eq(bookId));

        // when & then: HTTP DELETE 요청 수행 및 결과 검증
        mockMvc.perform(delete("/api/books/{bookId}/hard", bookId))
                .andExpect(status().isNoContent()); // HTTP 상태 코드가 204 No Content인지 확인
    }
}
