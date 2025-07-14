package com.twogether.deokhugam.book.controller;

import com.twogether.deokhugam.book.dto.BookDto;
import com.twogether.deokhugam.book.dto.request.BookCreateRequest;
import com.twogether.deokhugam.book.dto.request.BookUpdateRequest;
import com.twogether.deokhugam.book.service.BookService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/books")
@RequiredArgsConstructor
public class BookController {
	private final BookService bookService;

	/** 도서 등록
	 * 입력 :
	 * BookCreateRequest
	 * thumbnailImage
	 * 출력 :
	 * BookDTO
	 */
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<BookDto> register(
		@RequestPart("bookData") BookCreateRequest request,
		@RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage)
	{
		BookDto savedBook = bookService.registerBook(request);
//		if (thumbnailImage != null) {}  S3로 연결 저장 예정
		return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
	}
	/** 도서 수정
	 * 입력 :
	 * Bookid
	 * BookUpdateRequest
	 * thumbnailImage
	 * 출력 :
	 * BookDTO
	 */
	@PatchMapping(value = "/{bookId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<BookDto> update(
		@PathVariable("bookId") UUID bookId,
		@RequestPart("bookData") BookUpdateRequest request,
		@RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage)
	{
		BookDto updatedBook = bookService.updateBook(bookId, request);
//		if (thumbnailImage != null && !thumbnailImage.isEmpty()) {}  S3로 연결 저장 예정
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedBook);
	}

	/** 도서 목록 조회
	 * 출력 :
	 * List<BookDTO>
	 */
	@GetMapping
	public ResponseEntity<List<BookDto>> getAllBooks() {
		List<BookDto> result = bookService.getAllBooks();
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	/** 도서 조회
	 * 입력 :
	 * Bookid
	 * 출력 :
	 * BookDTO
	 */
	@GetMapping(value = "/{bookId}")
	public ResponseEntity<BookDto> getBook(
		@PathVariable("bookId") UUID bookId) {
		BookDto result = bookService.getBookbyId(bookId);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	/** 도서 논리삭제
	 * 입력 :
	 * Bookid
	 */
	@DeleteMapping(value = "/{bookId}")
	public ResponseEntity<Void> deleteBook(
		@PathVariable("bookId") UUID bookId
	){
		bookService.deleteBook(bookId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	/** 도서 물리삭제
	 * 입력 :
	 * Bookid
	 */
	@DeleteMapping(value = "/{bookId}/hard")
	public ResponseEntity<Void> deleteBookHard(
		@PathVariable("bookId") UUID bookId
	){
		bookService.deleteBookHard(bookId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
