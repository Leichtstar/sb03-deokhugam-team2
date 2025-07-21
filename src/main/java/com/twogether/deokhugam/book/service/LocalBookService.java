package com.twogether.deokhugam.book.service;

import com.twogether.deokhugam.book.dto.BookDto;
import com.twogether.deokhugam.book.dto.request.BookCreateRequest;
import com.twogether.deokhugam.book.dto.request.BookUpdateRequest;
import com.twogether.deokhugam.book.dto.response.BookPageResponse;
import com.twogether.deokhugam.book.entity.Book;
import com.twogether.deokhugam.book.exception.BookNotFoundException;
import com.twogether.deokhugam.book.exception.DuplicatedIsbnException;
import com.twogether.deokhugam.book.repository.BookRepository;
import com.twogether.deokhugam.review.repository.ReviewRepository;
import com.twogether.deokhugam.storage.S3ImageStorage;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class LocalBookService implements BookService {

    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final S3ImageStorage s3ImageStorage;

    @Override
    public BookDto registerBook(BookCreateRequest request)
    {
        if(bookRepository.existsByIsbn(request.isbn())){
            throw new DuplicatedIsbnException();
        }
        Book book = Book.of(request);
        return bookRepository.save(book).toDto();
    }
    @Override
    public BookDto registerBook(BookCreateRequest request,MultipartFile thumbnailImg)
    {
        if(bookRepository.existsByIsbn(request.isbn())){
            throw new DuplicatedIsbnException();
        }
        Book book = Book.of(request);
	    String imageUrl;
	    try {
		    imageUrl = s3ImageStorage.uploadImage(thumbnailImg, "bookThumbnail/");
	    } catch (IOException e) {
		    throw new RuntimeException("이미지 업로드 실패",e);
	    }
	    book.setThumbnailUrl(imageUrl);
        return bookRepository.save(book).toDto();
    }

    @Override
    public List<BookDto> getAllBooks() {
        return bookRepository.findAll().stream().filter(book -> !book.getIsDeleted()).map(Book::toDto).toList();
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
                // fallback: createdAt 기준
//                Instant afterTime = (after != null) ? after : Instant.EPOCH;
                results = isAsc
                    ? bookRepository.findBooksByKeywordAndAfterAsc(kw, after, pageable)
                    : bookRepository.findBooksByKeywordAndAfter(kw, after, pageable);
            }
        }

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

        // 반환
        return new BookPageResponse<>(
            results.stream().map(Book::toDto).toList(),
            nextCursor,
            nextAfter,
            results.size(),
            totalCount,
            hasNext
        );
}

    @Override
    public BookDto getBookbyId(UUID bookId) {
        Book targetbook = bookRepository.findById(bookId)
            .orElseThrow(BookNotFoundException::new);
        return targetbook.toDto();
    }


    public BookDto updateBook(UUID bookId, BookUpdateRequest request, @Nullable MultipartFile thumbnailImg) {
        Book targetbook = bookRepository.findById(bookId)
            .orElseThrow(BookNotFoundException::new);

        targetbook.setTitle(request.title());
        targetbook.setAuthor(request.author());
        targetbook.setDescription(request.description());
        targetbook.setPublisher(request.publisher());
        targetbook.setPublishedDate(request.publishedDate());

        if (thumbnailImg != null && !thumbnailImg.isEmpty()) {
            try {
                String imageUrl = s3ImageStorage.uploadImage(thumbnailImg, "bookThumbnail/");
                targetbook.setThumbnailUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("이미지 업로드 실패", e);
            }
        }
        return bookRepository.save(targetbook).toDto();
    }

    @Override
    public void deleteBook(UUID bookId){
        Book book = bookRepository.findById(bookId).
            orElseThrow(BookNotFoundException::new);
        book.setIsDeleted(true);
        bookRepository.save(book);
    }

    @Override
    public void deleteBookHard(UUID bookId){
        // 책 존재 확인 및 조회
        Book book = bookRepository.findById(bookId)
            .orElseThrow(BookNotFoundException::new);

        // 썸네일 URL 추출
        String thumbnailUrl = book.getThumbnailUrl(); // 예: https://your-bucket.s3.amazonaws.com/book/thumbnail/abc.jpg
        if (thumbnailUrl != null && !thumbnailUrl.isBlank()) {
            //s3에서 이미지 삭제 요청
            s3ImageStorage.deleteImage(thumbnailUrl);
        }
        bookRepository.deleteById(bookId);
    }

    @Override
    public void updateReviewStats(UUID bookId){
        Book book = bookRepository.findById(bookId).orElseThrow(BookNotFoundException::new);

        Object[] stats = (Object[]) reviewRepository.getReviewStats(bookId); // ✅ 캐스팅
        int count = ((Number) stats[0]).intValue();
        float average = ((Number) stats[1]).floatValue();

        book.setReviewCount(count);
        book.setRating(average);
    }
}
