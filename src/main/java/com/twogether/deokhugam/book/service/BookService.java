package com.twogether.deokhugam.book.service;

import com.twogether.deokhugam.book.dto.BookDto;
import com.twogether.deokhugam.book.dto.request.BookCreateRequest;
import com.twogether.deokhugam.book.dto.request.BookUpdateRequest;
import java.util.List;
import java.util.UUID;

public interface BookService {
    BookDto registerBook(BookCreateRequest request);
    BookDto getBookbyId(UUID bookId);
    List<BookDto> getAllBooks();
    BookDto modifyBook(UUID bookId, BookUpdateRequest request);
    void deleteBook(UUID bookId);
    void deleteBookHard(UUID bookId);
}
