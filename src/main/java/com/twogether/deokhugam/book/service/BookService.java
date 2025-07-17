package com.twogether.deokhugam.book.service;

import com.twogether.deokhugam.book.dto.BookDto;
import com.twogether.deokhugam.book.dto.request.BookCreateRequest;
import com.twogether.deokhugam.book.dto.request.BookUpdateRequest;
import com.twogether.deokhugam.book.dto.response.BookPageResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {
    BookDto registerBook(BookCreateRequest request);
    BookDto registerBook(BookCreateRequest request, MultipartFile thumbnailImg);
    BookDto getBookbyId(UUID bookId);
    List<BookDto> getAllBooks();
    BookPageResponse<BookDto> getAllSorted(String keyword,String orderBy, String Direction, String cursor, Instant createdAt, int limit);
    BookDto updateBook(UUID bookId, BookUpdateRequest request, MultipartFile thumbnailImg);
    void deleteBook(UUID bookId);
    void deleteBookHard(UUID bookId);
}
