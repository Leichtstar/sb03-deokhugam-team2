package com.twogether.deokhugam.book.controller;

import com.twogether.deokhugam.book.dto.BookDto;
import com.twogether.deokhugam.book.dto.request.BookCreateRequest;
import com.twogether.deokhugam.book.dto.request.BookUpdateRequest;
import com.twogether.deokhugam.book.dto.response.BookPageResponse;
import com.twogether.deokhugam.book.service.BookService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/books")
@RequiredArgsConstructor
public class BookController implements BookApi{
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
		@Validated @RequestPart("bookData") BookCreateRequest request,
		@RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage)
	{
		BookDto savedBook;
		if (thumbnailImage != null) {
			savedBook = bookService.registerBook(request, thumbnailImage);
		} else{
			savedBook = bookService.registerBook(request);
		}
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
		@Validated @RequestPart("bookData") BookUpdateRequest request,
		@RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage) {
		BookDto updatedBook;
		if (thumbnailImage != null && !thumbnailImage.isEmpty()){
			updatedBook = bookService.updateBook(bookId, request, thumbnailImage);

		}else {
			updatedBook = bookService.updateBook(bookId, request,null);
		}
		return ResponseEntity.status(HttpStatus.OK).body(updatedBook);
	}

	/** 도서 목록 조회
	 * 출력 :
	 * ResponseEntity<BookPageResponse<BookDto>>
	 */
	@GetMapping
	public ResponseEntity<BookPageResponse<BookDto>> getAllBooks(
		@RequestParam(required = false) String keyword,
		@RequestParam(defaultValue = "title") String orderBy,
		@RequestParam(defaultValue = "DESC") String direction,
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Instant after,
		@RequestParam(defaultValue = "30") int limit
	) {
		BookPageResponse<BookDto> result = bookService.getAllSorted(keyword,orderBy,direction,cursor,after,limit);
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
