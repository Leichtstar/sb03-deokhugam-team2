package com.twogether.deokhugam.book.controller;

import com.twogether.deokhugam.book.dto.BookDto;
import com.twogether.deokhugam.book.dto.request.BookCreateRequest;
import com.twogether.deokhugam.book.dto.request.BookUpdateRequest;
import com.twogether.deokhugam.book.dto.response.BookPageResponse;
import com.twogether.deokhugam.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.query.SortDirection;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "도서 관리", description = "도서 등록, 수정, 조회, 삭제 및 ISBN조회를 위한 외부 API")
public interface BookApi {

	@Operation(summary = "도서 등록", description = "새로운 도서를 등록합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "등록 성공",
			content = @Content(schema = @Schema(implementation = BookDto.class),
				examples = @ExampleObject(value = """
                    {
                      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                      "title": "도서 제목",
                      "author": "홍길동",
                      "isbn": "978-89-12345-67-8",
                      "thumbnailUrl": "https://s3.amazonaws.com/book-thumbnail.jpg",
                      "publishedDate": "2025-07-27T00:00:00Z"
                    }
                    """))),
		@ApiResponse(responseCode = "400", description = "잘못된 요청(입력값 검증 실패, ISBN 형식 오류 등)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(value = """
                    {
                      "message": "잘못된 요청입니다.",
                      "code": "BAD_REQUEST",
                      "status": 400
                    }
                    """))),
		@ApiResponse(responseCode = "409", description = "중복된 ISBN",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(value = """
                    {
                      "message": "이미 사용된 ISBN 코드입니다.",
                      "code": "DUPLICATED_ISBN",
                      "status": 409
                    }
                    """))),
		@ApiResponse(responseCode = "500", description = "서버 오류",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	ResponseEntity<BookDto> register(
		@Parameter(description = "도서 생성 정보", required = true,
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BookDto.class)))
		BookCreateRequest request,

		@Parameter(description = "썸네일 이미지 파일 (선택사항)", required = false,
			content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
		MultipartFile thumbnailImage
	);

	@Operation(summary = "도서 정보 수정", description = "기존 도서 정보를 수정합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "도서 정보 수정 성공",
			content = @Content(schema = @Schema(implementation = BookDto.class))),
		@ApiResponse(responseCode = "400", description = "잘못된 요청(입력값 검증 실패 등)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(value = """
                    {
                      "message": "잘못된 요청입니다.",
                      "code": "BAD_REQUEST",
                      "status": 400
                    }
                    """))),
		@ApiResponse(responseCode = "404", description = "도서를 찾을 수 없음",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(value = """
                    {
                      "message": "Book not found",
                      "code": "BOOK_NOT_FOUND",
                      "status": 404
                    }
                    """))),
		@ApiResponse(responseCode = "500", description = "서버 오류",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	ResponseEntity<BookDto> update(
		@Parameter(description = "도서 ID", example = "123e4567-e89b-12d3-a456-426614174000", required = true) UUID bookId,
		@Parameter(description = "도서 수정 정보", required = true,
			content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BookDto.class)))
		BookUpdateRequest request,
		@Parameter(description = "새로운 썸네일 이미지 파일 (선택사항)", required = false,
			content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
		MultipartFile thumbnailImage
	);

	@Operation(summary = "도서 목록 조회", description = "검색어, 정렬 조건, 페이징 옵션으로 도서 목록을 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "도서 목록 조회 성공",
			content = @Content(schema = @Schema(implementation = BookPageResponse.class),
				examples = @ExampleObject(value = """
                    {
                      "items": [
                        {
                          "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                          "title": "도서 제목",
                          "author": "홍길동",
                          "isbn": "978-89-12345-67-8"
                        }
                      ],
                      "nextCursor": "YWJjZDEyMw=="
                    }
                    """))),
		@ApiResponse(responseCode = "400", description = "잘못된 요청",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(value = """
                    {
                      "message": "잘못된 요청입니다.",
                      "code": "BAD_REQUEST",
                      "status": 400
                    }
                    """))),
		@ApiResponse(responseCode = "500", description = "서버 오류",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	ResponseEntity<BookPageResponse<BookDto>> getAllBooks(
		@Parameter(description = "검색어 (제목 | 저자 | ISBN)",example = "자바") String keyword,
		@Parameter(description = "정렬 기준(title | publishedDate | rating | reviewCount)", schema = @Schema(defaultValue = "title", example = "title")) String orderBy,
		@Parameter(description = "정렬 방향", schema = @Schema(implementation =SortDirection.class, defaultValue = "DESC", example = "DESC")) String direction,
		@Parameter(description = "커서 기반 페이징을 위한 커서 값") String cursor,
		@Parameter(description = "보조 커서 (created At)") Instant after,
		@Parameter(description = "페이지당 조회 개수(기본값: 30)") int limit
	);

	@Operation(summary = "도서 상세 조회", description = "도서의 상세 정보를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "조회 성공",
			content = @Content(schema = @Schema(implementation = BookDto.class))),
		@ApiResponse(responseCode = "404", description = "도서를 찾을 수 없음",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(value = """
                    {
                      "message": "Book not found",
                      "code": "BOOK_NOT_FOUND",
                      "status": 404
                    }
                    """))),
		@ApiResponse(responseCode = "500", description = "서버 오류",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	ResponseEntity<BookDto> getBook(@Parameter(description = "도서 ID", required = true) UUID bookId);

	@Operation(summary = "도서 삭제(논리 삭제)", description = "도서를 논리적으로 삭제합니다. (데이터는 유지되지만 상태가 '삭제됨'으로 변경)")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "삭제 성공"),
		@ApiResponse(responseCode = "404", description = "도서를 찾을 수 없음",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		@ApiResponse(responseCode = "500", description = "서버 오류",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	ResponseEntity<Void> deleteBook(@Parameter(description = "도서 ID", required = true) UUID bookId);

	@Operation(summary = "도서 완전 삭제(물리 삭제)", description = "데이터베이스에서 도서를 완전히 삭제합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "삭제 성공"),
		@ApiResponse(responseCode = "404", description = "도서를 찾을 수 없음",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
		@ApiResponse(responseCode = "500", description = "서버 오류",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	ResponseEntity<Void> deleteBookHard(@Parameter(description = "도서 ID", required = true) UUID bookId);
}
