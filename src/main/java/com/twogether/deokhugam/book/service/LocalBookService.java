package com.twogether.deokhugam.book.service;

import com.twogether.deokhugam.book.dto.BookDto;
import com.twogether.deokhugam.book.dto.request.BookCreateRequest;
import com.twogether.deokhugam.book.dto.request.BookUpdateRequest;
import com.twogether.deokhugam.book.entity.Book;
import com.twogether.deokhugam.book.repository.BookRepository;
import java.util.List;
import java.util.NoSuchElementException;
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
        return bookRepository.findAll().stream().filter(book -> !book.getIsDeleted()).map(Book::toDto).toList();
    }

    @Override
    public BookDto getBookbyId(UUID bookId) {
        Book targetbook = bookRepository.findById(bookId)
            .orElseThrow(() -> new NoSuchElementException("Book not found."));
        return targetbook.toDto();
    }

    @Override
    public BookDto updateBook(UUID bookId, BookUpdateRequest request) {
        Book targetbook = bookRepository.findById(bookId)
            .orElseThrow(() -> new NoSuchElementException("Book not found."));

        targetbook.setTitle(request.title());
        targetbook.setAuthor(request.author());
        targetbook.setDescription(request.description());
        targetbook.setPublisher(request.publisher());
        targetbook.setPublishedDate(request.publishedDate());

        return bookRepository.save(targetbook).toDto();
    }

    @Override
    public void deleteBook(UUID bookId){
        bookRepository.findById(bookId).ifPresent(book -> {
            book.setIsDeleted(true);
            bookRepository.save(book);
        });
    }

    @Override
    public void deleteBookHard(UUID bookId){
        if(!bookRepository.existsById(bookId)) {
            throw new NoSuchElementException("Book not found.");
        }
        bookRepository.deleteById(bookId);

    }


}
