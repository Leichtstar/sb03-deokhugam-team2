package com.twogether.deokhugam.book.service;

import com.twogether.deokhugam.book.dto.BookDto;
import com.twogether.deokhugam.book.dto.request.BookCreateRequest;
import com.twogether.deokhugam.book.dto.request.BookUpdateRequest;
import com.twogether.deokhugam.book.dto.response.BookPageResponse;
import com.twogether.deokhugam.book.entity.Book;
import com.twogether.deokhugam.book.exception.BookNotFoundException;
import com.twogether.deokhugam.book.exception.DuplicatedIsbnException;
import com.twogether.deokhugam.book.repository.BookRepository;
import com.twogether.deokhugam.storage.S3ImageStorage;
import jakarta.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final S3ImageStorage s3ImageStorage;

    @Override
    public BookDto registerBook(BookCreateRequest request)
    {
        log.info("[BookServiceImpl] 도서 등록 요청 (Thumbnail없음)");
        // ISBN 중복 여부 확인
        if(bookRepository.existsByIsbn(request.isbn())){
            log.warn("도서 등록 실패 - 중복 ISBN :{}",request.isbn());
            throw new DuplicatedIsbnException();
        }
        Book book = Book.of(request);
        BookDto registeredBook = bookRepository.save(book).toDto();
        log.info("도서 등록 성공 : 도서제목={}", registeredBook.title());

        return registeredBook;
    }
    @Override
    public BookDto registerBook(BookCreateRequest request,MultipartFile thumbnailImg)
    {
        log.info("[BookServiceImpl] 도서 등록 요청");
        // ISBN 중복 여부 확인
        if(bookRepository.existsByIsbn(request.isbn())){
            log.warn("도서 등록 실패 - 중복 ISBN :{}",request.isbn());
            throw new DuplicatedIsbnException();
        }
        Book book = Book.of(request);
        // 썸네일 이미지 S3로 업로드
	    String imageUrl;
        log.debug("S3에 썸네일 이미지 업로드 요청: {}",thumbnailImg.getOriginalFilename());

		imageUrl = s3ImageStorage.uploadImage(thumbnailImg, "bookThumbnail/");

	    book.setThumbnailUrl(imageUrl);
        BookDto registeredBook = bookRepository.save(book).toDto();

        log.info("도서 등록 성공 : {}", registeredBook.title());

        return registeredBook;
    }

    @Override
    public List<BookDto> getAllBooks() {
        log.info("[BookServiceImpl] 모든 도서 리스트 조회 요청.");
        List<BookDto> result = bookRepository.findAll().stream().filter(book -> !book.getIsDeleted()).map(Book::toDto).toList();
        log.info("모든 도서 리스트 조회 성공: 전체 도서 수량={}" , result.size());
        return result;
    }
    @Override
    public BookPageResponse<BookDto> getAllSorted(
        String keyword,
        String orderBy,
        String direction,
        String cursor,
        Instant after,
        int limit
    ) {
        log.info("[BookServiceImpl] 도서목록 정렬조회 요청 : 검색어={}",keyword);
        log.debug("정렬기준={}, 정렬방향={}",orderBy,direction);
        String kw = (keyword == null || keyword.isBlank()) ? "" : keyword;

        // 페이징용 Pageable 생성
        Pageable pageable = PageRequest.of(0, limit + 1);


        List<Book> results;
        boolean isAsc = "asc".equalsIgnoreCase(direction);
        switch (orderBy) {
            case "title" -> {
                results = isAsc
                    ? bookRepository.findPageByTitleAsc(kw, cursor, after, pageable)
                    : bookRepository.findPageByTitleDesc(kw, cursor, after, pageable);
            }
            case "publishedDate" -> {
                LocalDate cursorDate = null;
                if (cursor != null) {
                    try {
                        cursorDate = LocalDate.parse(cursor);
                    } catch (DateTimeParseException e) {
                        throw new IllegalArgumentException("잘못된 날짜 형식입니다: " + cursor, e);
                    }
                }
                results = isAsc
                    ? bookRepository.findPageByPublishedDateAsc(kw, cursorDate, after, pageable)
                    : bookRepository.findPageByPublishedDateDesc(kw, cursorDate, after, pageable);
            }
            case "rating" -> {
                Float cursorRating = (cursor != null) ? Float.parseFloat(cursor) : null;
                results = isAsc
                    ? bookRepository.findPageByRatingAsc(kw, cursorRating, after, pageable)
                    : bookRepository.findPageByRatingDesc(kw, cursorRating, after, pageable);
            }
            case "reviewCount" -> {
                Integer cursorReviewCount = (cursor != null) ? Integer.parseInt(cursor) : null;
                results = isAsc
                    ? bookRepository.findPageByReviewCountAsc(kw, cursorReviewCount, after, pageable)
                    : bookRepository.findPageByReviewCountDesc(kw, cursorReviewCount, after, pageable);
            }
            default -> {
                results = isAsc
                    ? bookRepository.findBooksByKeywordAndAfterAsc(kw, after, pageable)
                    : bookRepository.findBooksByKeywordAndAfter(kw, after, pageable);
            }
        }
        log.debug("Pageable 쿼리 조회 성공 : pagesize={}", pageable.getPageSize());
        boolean hasNext = results.size() > limit;
        if (hasNext) {
            results = results.subList(0, limit);
        }

        // 커서 정보 계산
        String nextCursor = null;
        Instant nextAfter = null;

        if (hasNext && !results.isEmpty()) {
            Book last = results.get(results.size() - 1);
            nextAfter = last.getCreatedAt();

            // 모든 정렬 기준에서 커서를 String으로 반환
            Object cursorValue = switch (orderBy) {
                case "title" -> last.getTitle();
                case "publishedDate" -> last.getPublishedDate();
                case "rating" -> last.getRating();
                case "reviewCount" -> last.getReviewCount();
                default -> last.getCreatedAt();  // fallback
            };

            nextCursor = (cursorValue != null) ? cursorValue.toString() : null;
        }

        //전체 목록 카운트
        long totalCount = bookRepository.countByKeyword(kw);

        // 결과값 생성
        BookPageResponse<BookDto> resultPage = new BookPageResponse<>(
            results.stream().map(Book::toDto).toList(),
            nextCursor,
            nextAfter,
            results.size(),
            totalCount,
            hasNext
        );
        log.info("도서목록 정렬조회 성공: totalElements={}",resultPage.totalElements());
        return resultPage;
}

    @Override
    public BookDto getBookbyId(UUID bookId) {
        log.info("[BookServiceImpl] 도서정보 단일조회 요청 : BookId={}", bookId);
        Book targetbook = bookRepository.findById(bookId)
            .orElseThrow(BookNotFoundException::new);
        BookDto result = targetbook.toDto();
        log.info("도서정보 단일조회 성공 : title={}",result.title());
        return result;
    }


    public BookDto updateBook(UUID bookId, BookUpdateRequest request, @Nullable MultipartFile thumbnailImg) {
        log.info("[BookServiceImpl] 도서정보 수정 요청 : BookId={}",bookId);
        Book targetbook = bookRepository.findById(bookId)
            .orElseThrow(BookNotFoundException::new);

        targetbook.setTitle(request.title());
        targetbook.setAuthor(request.author());
        targetbook.setDescription(request.description());
        targetbook.setPublisher(request.publisher());
        targetbook.setPublishedDate(request.publishedDate());


        if (thumbnailImg != null && !thumbnailImg.isEmpty()) {
            log.debug("S3 썸네일 교체 요청");
            String imageUrl = s3ImageStorage.uploadImage(thumbnailImg, "bookThumbnail/");
            targetbook.setThumbnailUrl(imageUrl);
            log.debug("S3 썸네일 교체 성공: url={}", imageUrl);
        } else {
            log.debug("S3 썸네일 변경 없음.");
        }
        BookDto result = bookRepository.save(targetbook).toDto();
        log.info("도서정보 수정 성공: BookId={}",bookId);
        return result;
    }

    @Override
    public void deleteBook(UUID bookId){
        log.info("[BookServiceImpl] 도서정보 논리삭제 요청 : BookId={}", bookId);
        // 책 존재 확인 및 조회
        Book book = bookRepository.findById(bookId).
            orElseThrow(BookNotFoundException::new);
        // 논리삭제 체크 활성화
        book.setIsDeleted(true);
        bookRepository.save(book);
        log.info("도서정보 논리삭제 성공 : BookId={}", bookId);
    }

    @Override
    public void deleteBookHard(UUID bookId){
        log.info("[BookServiceImpl] 도서정보 물리삭제 요청 : BookId={}", bookId);
        // 책 존재 확인 및 조회
        Book book = bookRepository.findById(bookId)
            .orElseThrow(BookNotFoundException::new);
        // 썸네일 URL 추출
        String thumbnailUrl = book.getThumbnailUrl();
        //s3 썸네일 삭제
        log.debug("S3 연동 썸네일 삭제 요청");
        s3ImageStorage.deleteImage(thumbnailUrl);

        bookRepository.deleteById(bookId);
        log.info("도서정보 물리삭제 성공 : BookId={}",bookId);
    }
}
