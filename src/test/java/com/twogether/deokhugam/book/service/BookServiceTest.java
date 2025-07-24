package com.twogether.deokhugam.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.twogether.deokhugam.book.dto.BookDto;
import com.twogether.deokhugam.book.dto.request.BookCreateRequest;
import com.twogether.deokhugam.book.dto.request.BookUpdateRequest;
import com.twogether.deokhugam.book.dto.response.BookPageResponse;
import com.twogether.deokhugam.book.entity.Book;
import com.twogether.deokhugam.book.exception.BookNotFoundException;
import com.twogether.deokhugam.book.exception.DuplicatedIsbnException;
import com.twogether.deokhugam.book.repository.BookRepository;
import com.twogether.deokhugam.storage.S3ImageStorage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

  @InjectMocks // 테스트 대상인 BookServiceImpl에 Mock 객체 주입
  private BookServiceImpl bookService;

  @Mock // BookServiceImpl이 의존하는 BookRepository를 Mock 객체로 생성
  private BookRepository bookRepository;

  @Mock // BookServiceImpl이 의존하는 S3ImageStorage를 Mock 객체로 생성
  private S3ImageStorage s3ImageStorage;

  private Book book; // 테스트에 사용될 Book 엔티티
  private BookCreateRequest createRequest; // 테스트에 사용될 BookCreateRequest
  private BookUpdateRequest updateRequest; // 테스트에 사용될 BookUpdateRequest

  @BeforeEach // 각 테스트 메서드 실행 전 초기화
  void setUp() {
    // 테스트용 Book 엔티티 및 DTO 초기화
    book = new Book(
        UUID.randomUUID(),
        "더쿠의 심리학",
        "박인규",
        "더쿠에 대한 심도깊은 해설",
        "이북리더즈",
        LocalDate.of(1989, 5, 12)
    );


    createRequest = new BookCreateRequest(
        "더쿠의 심리학",
        "박인규",
        "더쿠에 대한 심도깊은 해설",
        "이북리더즈",
        LocalDate.of(1989, 5, 12),
        null // ISBN 없음
    );

    updateRequest = new BookUpdateRequest(
        "업데이트된 도서",
        "업데이트된 저자",
        "업데이트된 설명",
        "업데이트된 출판사",
        LocalDate.of(2024, 1, 1)
    );
  }

  @Test
  @DisplayName("도서 등록 성공 테스트 - 썸네일 이미지 없음,ISBN 없음")
  void registerBookWithoutThumbnailSuccess() throws IOException {
    // given: ISBN 중복 없음, bookRepository.save 호출 시 book 반환
    // given(bookRepository.existsByIsbn(anyString())).willReturn(false);
    given(bookRepository.save(any(Book.class))).willReturn(book);

    // when: 썸네일 이미지 없이 도서 등록
    BookDto result = bookService.registerBook(createRequest);

    // then: 결과 검증 및 메서드 호출 검증
    assertThat(result).isNotNull();
    assertThat(result.title()).isEqualTo(createRequest.title());
    assertThat(result.isbn()).isEqualTo(createRequest.isbn());
//    verify(bookRepository, times(1)).existsByIsbn(anyString()); // existsByIsbn 호출 확인
    verify(bookRepository, times(1)).save(any(Book.class)); // save 호출 확인
    verify(s3ImageStorage, never()).uploadImage(any(), anyString()); // uploadImage 호출 안됨 확인
  }

  @Test
  @DisplayName("도서 등록 성공 테스트 - 썸네일 이미지 포함")
  void registerBookWithThumbnailSuccess() throws IOException {
    // given
    MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "test.jpg", "image/jpeg", "test data".getBytes());
    given(bookRepository.existsByIsbn(any())).willReturn(false);
    given(s3ImageStorage.uploadImage(any(MultipartFile.class), anyString()))
        .willReturn("http://new-thumbnail.url");
    given(bookRepository.save(any(Book.class)))
        .willAnswer(invocation -> invocation.getArgument(0)); // 핵심

    // when
    BookDto result = bookService.registerBook(createRequest, thumbnail);

    // then
    assertThat(result.thumbnailUrl()).isEqualTo("http://new-thumbnail.url");
    verify(s3ImageStorage, times(1)).uploadImage(any(MultipartFile.class), anyString());
  }


  @Test
  @DisplayName("도서 등록 실패 테스트 - ISBN 중복")
  void registerBookFail_DuplicatedIsbn() {
    // given: ISBN 중복 발생
    given(bookRepository.existsByIsbn(any())).willReturn(true);

    // when & then: DuplicatedIsbnException 발생 확인
    assertThrows(DuplicatedIsbnException.class, () -> bookService.registerBook(createRequest));
    verify(bookRepository, times(1)).existsByIsbn(any());
    verify(bookRepository, never()).save(any(Book.class)); // save 호출 안됨 확인
  }

  @Test
  @DisplayName("모든 도서 조회 성공 테스트")
  void getAllBooksSuccess() {
    // given: 삭제되지 않은 도서 목록 반환
    List<Book> books = Arrays.asList(book, new Book("도서2", "", "", "", LocalDate.now()));
    given(bookRepository.findAll()).willReturn(books);

    // when: 모든 도서 조회
    List<BookDto> result = bookService.getAllBooks();

    // then: 결과 검증
    assertThat(result).hasSize(2);
    assertThat(result.get(0).title()).isEqualTo(book.getTitle());
    verify(bookRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("정렬 옵션별 도서 목록 조회 성공 테스트 - 모든 분기 커버")
  void getAllSorted_AllSortOptions_Success() {
    // given
    List<Book> books = Arrays.asList(
        new Book("가나다", "", "", "", LocalDate.now()),
        new Book("마바사", "", "", "", LocalDate.now()),
        new Book("추가책", "", "", "", LocalDate.now())
    );

    // 공통 Mock 설정
    given(bookRepository.countByKeyword(anyString())).willReturn(3L);

    // 각 정렬별 Mock 설정
    given(bookRepository.findPageByTitleAsc(anyString(), any(), any(), any(Pageable.class))).willReturn(books);
    given(bookRepository.findPageByTitleDesc(anyString(), any(), any(), any(Pageable.class))).willReturn(books);
    given(bookRepository.findPageByReviewCountAsc(anyString(), any(), any(), any(Pageable.class))).willReturn(books);
    given(bookRepository.findPageByReviewCountDesc(anyString(), any(), any(), any(Pageable.class))).willReturn(books);
    given(bookRepository.findPageByRatingAsc(anyString(), any(), any(), any(Pageable.class))).willReturn(books);
    given(bookRepository.findPageByRatingDesc(anyString(), any(), any(), any(Pageable.class))).willReturn(books);
    given(bookRepository.findPageByPublishedDateAsc(anyString(), any(), any(), any(Pageable.class))).willReturn(books);
    given(bookRepository.findPageByPublishedDateDesc(anyString(), any(), any(), any(Pageable.class))).willReturn(books);
    given(bookRepository.findBooksByKeywordAndAfterAsc(anyString(), any(), any(Pageable.class))).willReturn(books);
    given(bookRepository.findBooksByKeywordAndAfter(anyString(), any(), any(Pageable.class))).willReturn(books);

    // when
    BookPageResponse<BookDto> resultReviewAsc = bookService.getAllSorted("", "reviewCount", "ASC", null, null, 2);
    BookPageResponse<BookDto> resultReviewDesc = bookService.getAllSorted("", "reviewCount", "DESC", null, null, 2);
    BookPageResponse<BookDto> resultRatingAsc = bookService.getAllSorted("", "rating", "ASC", null, null, 2);
    BookPageResponse<BookDto> resultRatingDesc = bookService.getAllSorted("", "rating", "DESC", null, null, 2);
    BookPageResponse<BookDto> resultPubDateAsc = bookService.getAllSorted("", "publishedDate", "ASC", null, null, 2);
    BookPageResponse<BookDto> resultPubDateDesc = bookService.getAllSorted("", "publishedDate", "DESC", null, null, 2);
    BookPageResponse<BookDto> resultTitleAsc = bookService.getAllSorted("", "title", "ASC", null, null, 2);
    BookPageResponse<BookDto> resultTitleDesc = bookService.getAllSorted("", "title", "DESC", null, null, 2);
    BookPageResponse<BookDto> resultDefaultAsc = bookService.getAllSorted("", "", "ASC", null, null, 2);
    BookPageResponse<BookDto> resultDefaultDesc = bookService.getAllSorted("", "", "DESC", null, null, 2);

    // then: 공통 검증
    assertThat(resultTitleAsc.content()).hasSize(2);
    assertThat(resultTitleAsc.content().get(0).title()).isEqualTo("가나다");
    assertThat(resultTitleAsc.nextCursor()).isEqualTo("마바사");
    assertThat(resultTitleAsc.totalElements()).isEqualTo(3L);

    // then: 분기별 메서드 호출 검증
    verify(bookRepository, times(1)).findPageByReviewCountAsc(anyString(), any(), any(), any(Pageable.class));
    verify(bookRepository, times(1)).findPageByReviewCountDesc(anyString(), any(), any(), any(Pageable.class));
    verify(bookRepository, times(1)).findPageByRatingAsc(anyString(), any(), any(), any(Pageable.class));
    verify(bookRepository, times(1)).findPageByRatingDesc(anyString(), any(), any(), any(Pageable.class));
    verify(bookRepository, times(1)).findPageByPublishedDateAsc(anyString(), any(), any(), any(Pageable.class));
    verify(bookRepository, times(1)).findPageByPublishedDateDesc(anyString(), any(), any(), any(Pageable.class));
    verify(bookRepository, times(1)).findPageByTitleAsc(anyString(), any(), any(), any(Pageable.class));
    verify(bookRepository, times(1)).findPageByTitleDesc(anyString(), any(), any(), any(Pageable.class));
    verify(bookRepository, times(1)).findBooksByKeywordAndAfterAsc(anyString(), any(), any(Pageable.class));
    verify(bookRepository, times(1)).findBooksByKeywordAndAfter(anyString(), any(), any(Pageable.class));
  }


  @Test
  @DisplayName("단일 도서 조회 성공 테스트")
  void getBookByIdSuccess() {
    // given: bookId로 도서 조회 시 book 반환

    given(bookRepository.findById(any(UUID.class))).willReturn(Optional.of(book));

    // when: 단일 도서 조회
    BookDto result = bookService.getBookbyId(book.getId());

    // then: 결과 검증
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(book.getId());
    verify(bookRepository, times(1)).findById(any(UUID.class));
  }

  @Test
  @DisplayName("단일 도서 조회 실패 테스트 - 도서 없음")
  void getBookByIdFail_BookNotFound() {
    // given: bookId로 도서 조회 시 Optional.empty 반환
    given(bookRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    // when & then: BookNotFoundException 발생 확인
    assertThrows(BookNotFoundException.class, () -> bookService.getBookbyId(UUID.randomUUID()));
    verify(bookRepository, times(1)).findById(any(UUID.class));
  }

  @Test
  @DisplayName("도서 정보 업데이트 성공 테스트 - 썸네일 이미지 포함")
  void updateBookWithThumbnailSuccess() throws IOException {
    // given: bookId로 도서 조회 시 book 반환, S3 이미지 업로드 성공
    MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "updated.jpg", "image/jpeg", "updated data".getBytes());
    given(bookRepository.findById(any(UUID.class))).willReturn(Optional.of(book));
    given(s3ImageStorage.uploadImage(any(MockMultipartFile.class), anyString())).willReturn("http://updated-thumbnail.url");
    given(bookRepository.save(any(Book.class))).willReturn(book);

    // when: 썸네일 이미지와 함께 도서 정보 업데이트
    BookDto result = bookService.updateBook(book.getId(), updateRequest, thumbnail);

    // then: 결과 검증 및 메서드 호출 검증
    assertThat(result).isNotNull();
    assertThat(result.title()).isEqualTo(updateRequest.title());
    assertThat(result.thumbnailUrl()).isEqualTo("http://updated-thumbnail.url");
    verify(bookRepository, times(1)).findById(any(UUID.class));
    verify(s3ImageStorage, times(1)).uploadImage(any(MockMultipartFile.class), anyString());
    verify(bookRepository, times(1)).save(any(Book.class));
  }

  @Test
  @DisplayName("도서 정보 업데이트 성공 테스트 - 썸네일 이미지 없음")
  void updateBookWithoutThumbnailSuccess() throws IOException {
    // given: bookId로 도서 조회 시 book 반환
    given(bookRepository.findById(any(UUID.class))).willReturn(Optional.of(book));
    given(bookRepository.save(any(Book.class))).willReturn(book);

    // when: 썸네일 이미지 없이 도서 정보 업데이트
    BookDto result = bookService.updateBook(book.getId(), updateRequest, null);

    // then: 결과 검증 및 메서드 호출 검증
    assertThat(result).isNotNull();
    assertThat(result.title()).isEqualTo(updateRequest.title());
    assertThat(result.thumbnailUrl()).isEqualTo(book.getThumbnailUrl()); // 기존 썸네일 유지
    verify(bookRepository, times(1)).findById(any(UUID.class));
    verify(s3ImageStorage, never()).uploadImage(any(), anyString()); // uploadImage 호출 안됨 확인
    verify(bookRepository, times(1)).save(any(Book.class));
  }

  @Test
  @DisplayName("도서 정보 업데이트 실패 테스트 - 도서 없음")
  void updateBookFail_BookNotFound() {
    // given: bookId로 도서 조회 시 Optional.empty 반환
    given(bookRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    // when & then: BookNotFoundException 발생 확인
    assertThrows(BookNotFoundException.class, () -> bookService.updateBook(UUID.randomUUID(), updateRequest, null));
    verify(bookRepository, times(1)).findById(any(UUID.class));
    verify(bookRepository, never()).save(any(Book.class));
  }

  @Test
  @DisplayName("도서 논리 삭제 성공 테스트")
  void deleteBookSuccess() {
    // given: bookId로 도서 조회 시 book 반환
    given(bookRepository.findById(any(UUID.class))).willReturn(Optional.of(book));
    given(bookRepository.save(any(Book.class))).willReturn(book);

    // when: 도서 논리 삭제
    bookService.deleteBook(book.getId());

    // then: isDeleted가 true로 설정되었는지 확인
    assertThat(book.getIsDeleted()).isTrue();
    verify(bookRepository, times(1)).findById(any(UUID.class));
    verify(bookRepository, times(1)).save(any(Book.class));
  }

  @Test
  @DisplayName("도서 논리 삭제 실패 테스트 - 도서 없음")
  void deleteBookFail_BookNotFound() {
    // given: bookId로 도서 조회 시 Optional.empty 반환
    given(bookRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    // when & then: BookNotFoundException 발생 확인
    assertThrows(BookNotFoundException.class, () -> bookService.deleteBook(UUID.randomUUID()));
    verify(bookRepository, times(1)).findById(any(UUID.class));
    verify(bookRepository, never()).save(any(Book.class));
  }

  @Test
  @DisplayName("도서 물리 삭제 성공 테스트 - 썸네일 이미지 포함")
  void deleteBookHardWithThumbnailSuccess() {
    // given: bookId로 도서 조회 시 book 반환, 썸네일 URL 존재
    book.setThumbnailUrl("http://updated-thumbnail.url");
    given(bookRepository.findById(any(UUID.class))).willReturn(Optional.of(book));
    doNothing().when(s3ImageStorage).deleteImage(anyString());
    doNothing().when(bookRepository).deleteById(any(UUID.class));

    // when: 도서 물리 삭제
    bookService.deleteBookHard(book.getId());

    // then: S3 이미지 삭제 및 도서 삭제 메서드 호출 확인
    verify(bookRepository, times(1)).findById(any(UUID.class));
    verify(s3ImageStorage, times(1)).deleteImage(anyString());
    verify(bookRepository, times(1)).deleteById(any(UUID.class));
  }

  @Test
  @DisplayName("도서 물리 삭제 성공 테스트 - 썸네일 이미지 없음")
  void deleteBookHardWithoutThumbnailSuccess() {
    // given: bookId로 도서 조회 시 book 반환, 썸네일 URL 없음
    book.setThumbnailUrl(null);
    given(bookRepository.findById(any(UUID.class))).willReturn(Optional.of(book));
    doNothing().when(bookRepository).deleteById(any(UUID.class));

    // when: 도서 물리 삭제
    bookService.deleteBookHard(book.getId());

    // then: S3 이미지 삭제 메서드 호출 안됨 및 도서 삭제 메서드 호출 확인
    verify(bookRepository, times(1)).findById(any(UUID.class));
    verify(s3ImageStorage, never()).deleteImage(anyString()); // 썸네일 없으므로 호출 안됨
    verify(bookRepository, times(1)).deleteById(any(UUID.class));
  }

  @Test
  @DisplayName("도서 물리 삭제 실패 테스트 - 도서 없음")
  void deleteBookHardFail_BookNotFound() {
    // given: bookId로 도서 조회 시 Optional.empty 반환
    given(bookRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    // when & then: BookNotFoundException 발생 확인
    assertThrows(BookNotFoundException.class, () -> bookService.deleteBookHard(UUID.randomUUID()));
    verify(bookRepository, times(1)).findById(any(UUID.class));
    verify(bookRepository, never()).deleteById(any(UUID.class));
  }
}
