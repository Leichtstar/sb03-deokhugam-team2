package com.twogether.deokhugam.book.service;

import com.twogether.deokhugam.book.dto.BookDto;
import com.twogether.deokhugam.book.dto.request.BookCreateRequest;
import com.twogether.deokhugam.book.dto.request.BookUpdateRequest;
import com.twogether.deokhugam.book.entity.Book;
import com.twogether.deokhugam.book.repository.BookRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocalBookService implements BookService {

  private final BookRepository bookRepository;


  @Override
  public BookDto registerBook(BookCreateRequest request) {
    Book book = new Book(
        request.title(),
        request.author(),
        request.description(),
        request.publisher(),
        request.publishedDate()
    );
    if (request.isbn() != null && !request.isbn().isBlank()) {
      book.setIsbn(request.isbn());
    }
    return bookRepository.save(book).toDto();
  }

  @Override
  public List<BookDto> getAllBooks() {
    return bookRepository.findAll().stream().map(Book::toDto).toList();
  }

  @Override
  public BookDto modifyBook(UUID bookId, BookUpdateRequest request) {
    Book book = new Book(
        request.title(),
        request.author(),
        request.description(),
        request.publisher(),
        request.publishedDate()
    );
    return bookRepository.save(book).toDto();
  }

  @Override
  public void deleteBook(UUID bookId){
    bookRepository.findById(bookId).ifPresent(book -> book.setIsDeleted(true));
  }

  @Override
  public void deleteBookHard(UUID bookId){
    bookRepository.deleteById(bookId);
  }


}
