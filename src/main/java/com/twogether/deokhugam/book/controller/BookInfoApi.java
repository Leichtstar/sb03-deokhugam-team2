package com.twogether.deokhugam.book.controller;

import com.twogether.deokhugam.book.dto.NaverBookDto;
import com.twogether.deokhugam.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "도서 관리")
public interface BookInfoApi {

	@Operation(summary = "ISBN으로 도서 정보 조회",
		description = "ISBN(10 또는 13자리)을 이용해 네이버 도서 API에서 정보를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "조회 성공",
			content = @Content(schema = @Schema(implementation = NaverBookDto.class),
				examples = @ExampleObject(value = """
                    {
                      "title": "도서 제목",
                      "author": "홍길동",
                      "isbn": "9788960777330",
                      "publisher": "출판사명",
                      "pubDate": "2025-07-27"
                    }
                    """))),
		@ApiResponse(responseCode = "400", description = "잘못된 ISBN 형식",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(value = """
                    {
                      "message": "Invalid ISBN format",
                      "code": "INVALID_ISBN",
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
		@ApiResponse(responseCode = "500", description = "외부 API 연동 오류",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	ResponseEntity<NaverBookDto> getBookInfo(
		@Parameter(description = "ISBN (10자리 또는 13자리)", example = "9788960777330", required = true)
		String isbn
	);

	@Operation(summary = "이미지 기반 ISBN 인식",
		description = "책 표지 이미지를 업로드하여 ISBN을 추출합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "추출 성공",
			content = @Content(schema = @Schema(implementation = String.class),
				examples = @ExampleObject(value = "\"9788960777330\""))),
		@ApiResponse(responseCode = "400", description = "잘못된 이미지 파일 또는 인식 실패",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(value = """
                    {
                      "message": "Invalid image file",
                      "code": "INVALID_IMAGE_FILE",
                      "status": 400
                    }
                    """))),
		@ApiResponse(responseCode = "500", description = "서버 내부 오류로 처리 실패",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	ResponseEntity<String> getIsbnFromOcr(
		@Parameter(description = "책 표지 이미지 파일", required = true)
		@RequestParam("image") MultipartFile image
	);
}
