package com.twogether.deokhugam.review.controller;

import com.twogether.deokhugam.common.dto.CursorPageResponseDto;
import com.twogether.deokhugam.review.dto.ReviewDto;
import com.twogether.deokhugam.review.dto.ReviewLikeDto;
import com.twogether.deokhugam.review.dto.request.ReviewCreateRequest;
import com.twogether.deokhugam.review.dto.request.ReviewUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "리뷰 관리", description = "리뷰 관련 API")
public interface ReviewApi {

    // 리뷰 생성
    @Operation(
            summary = "리뷰 등록",
            description = "새로운 리뷰를 등록합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", description = "리뷰 등록 성공",
                    content = @Content(schema = @Schema(implementation = ReviewDto.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                                "timestamp": "2025-07-25T01:05:34.294333Z",
                                "code": "INVALID_INPUT_VALUE",
                                "message": "잘못된 입력값입니다.",
                                "details": {
                                    "rating": "리뷰 평점은 1점 이상이어야 합니다."
                                },
                                "exceptionType": "MethodArgumentNotValidException",
                                "status": 400
                            }
                        """))
            ),
            @ApiResponse(
                    responseCode = "404", description = "도서 정보 없음",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                                "timestamp": "2025-07-25T01:08:52.777917300Z",
                                "code": "BOOK_NOT_FOUND",
                                "message": "등록되지 않은 도서입니다.",
                                "details": {},
                                "exceptionType": "BookNotFoundException",
                                "status": 404
                            }
                            """))
            ),
            @ApiResponse(
                    responseCode = "409", description = "이미 작성된 리뷰 존재",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                                "timestamp": "2025-07-25T01:09:27.812650900Z",
                                "code": "REVIEW_ALREADY_EXISTS",
                                "message": "이미 작성한 리뷰가 있습니다.",
                                "details": {},
                                "exceptionType": "ReviewExistException",
                                "status": 409
                            }
                            """))
            ),
            @ApiResponse(
                    responseCode = "500", description = "서버 내부 오류",
                    content = @Content(examples = @ExampleObject(value = "리뷰 등록 중 서버 오류가 발생했습니다."))
            )}

    )
    @PostMapping
    ResponseEntity<ReviewDto> createReview(
            @Parameter(description = "리뷰 생성 정보")
            @Valid @RequestBody ReviewCreateRequest request
    );


    // 리뷰 상세 조회
    @Operation(
            summary = "리뷰 상세 정보 조회",
            description = "리뷰 ID로 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "리뷰 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = ReviewDto.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                                "timestamp": "2025-07-25T01:10:42.804368500Z",
                                "code": "MISSING_HEADER",
                                "message": "필수 헤더가 누락되었습니다: Deokhugam-Request-User-ID",
                                "details": null,
                                "exceptionType": "MissingRequestHeaderException",
                                "status": 400
                            }
                            """))
            ),
            @ApiResponse(
                    responseCode = "404", description = "리뷰 정보 없음",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                                "timestamp": "2025-07-25T01:13:30.906575900Z",
                                "code": "REVIEW_NOT_FOUND",
                                "message": "리뷰를 찾을 수 없습니다.",
                                "details": {
                                    "조회하려고 한 리뷰 아이디": "1364cc9c-ef00-4680-8e93-c8c72dc6d8a3"
                                },
                                "exceptionType": "ReviewNotFoundException",
                                "status": 404
                            }
                            """))
            ),
            @ApiResponse(
                    responseCode = "500", description = "서버 내부 오류",
                    content = @Content(examples = @ExampleObject(value = "리뷰 상세 조회 중 서버 오류가 발생했습니다."))
            )
    })
    @GetMapping("/{reviewId}")
    ResponseEntity<ReviewDto> getReview(
            @Parameter(
                    name = "reviewId", description = "리뷰 ID", required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID reviewId,

            @Parameter(
                    name = "Deokhugam-Request-User-ID", description = "요청자 ID", required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    );

    // 리뷰 목록 조회
    @Operation(
            summary = "리뷰 목록 조회",
            description = "검색 조건에 맞는 리뷰 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "리뷰 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = CursorPageResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류, 요청자 ID 누락)",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                                "timestamp": "2025-07-25T01:14:55.436125300Z",
                                "code": "MISSING_HEADER",
                                "message": "필수 헤더가 누락되었습니다: Deokhugam-Request-User-ID",
                                "details": null,
                                "exceptionType": "MissingRequestHeaderException",
                                "status": 400
                            }
                            """))
            ),
            @ApiResponse(
                    responseCode = "500", description = "서버 내부 오류",
                    content = @Content(examples = @ExampleObject(value = "리뷰 목록 조회 중 서버 오류가 발생했습니다."))
            )
    })
    @GetMapping
    ResponseEntity<CursorPageResponseDto<ReviewDto>> searchReviews(
            @Parameter(
                    name = "userId", description = "작성자 ID",
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestParam(required = false) UUID userId,
            @Parameter(
                    name = "bookId", description = "도서 ID",
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestParam(required = false) UUID bookId,
            @Parameter(
                    name = "keyword", description = "검색 키워드 (작성자 닉네임 | 내용)",
                    example = "홍길동"
            )
            @RequestParam(required = false) String keyword,
            @Parameter(
                    name = "orderBy", description = "정렬 기준(createdAt | rating)",
                    example = "createdAt",
                    schema = @Schema(allowableValues = {"createdAt", "rating"})
            )
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @Parameter(
                    name = "direction", description = "정렬 방향",
                    example = "DESC",
                    schema = @Schema(allowableValues = {"ASC", "DESC"})
            )
            @RequestParam(defaultValue = "DESC") String direction,
            @Parameter(
                    name = "cursor", description = "커서 페이지네이션 커서"
            )
            @RequestParam(required = false) String cursor,
            @Parameter(
                    name = "after", description = "보조 커서(createdAt)",
                    example = "2024-05-01T12:34:56"
            )
            @RequestParam(required = false) String after,
            @Parameter(
                    name = "limit", description = "페이지 크기",
                    example = "50"
            )
            @RequestParam(defaultValue = "50") int limit,
            @Parameter(
                    name = "Deokhugam-Request-User-ID", description = "요청자 ID", required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    );

    // 리뷰 수정
    @Operation(
            summary = "리뷰 수정",
            description = "본인이 작성한 리뷰를 수정합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "리뷰 수정 성공",
                    content = @Content(schema = @Schema(implementation = ReviewDto.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                                "timestamp": "2025-07-25T01:18:43.259248700Z",
                                "code": "INVALID_INPUT_VALUE",
                                "message": "잘못된 입력값입니다.",
                                "details": {
                                    "content": "리뷰 내용은 필수 입력 항목입니다."
                                },
                                "exceptionType": "MethodArgumentNotValidException",
                                "status": 400
                            }
                            """))
            ),
            @ApiResponse(
                    responseCode = "403", description = "리뷰 수정 권한 없음",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                                "timestamp": "2025-07-25T01:18:15.235684800Z",
                                "code": "REVIEW_NOT_OWNED",
                                "message": "본인이 작성한 리뷰만 수정/삭제할 수 있습니다.",
                                "details": {},
                                "exceptionType": "ReviewNotOwnedException",
                                "status": 403
                            }
                            """))
            ),
            @ApiResponse(
                    responseCode = "404", description = "리뷰 정보 없음",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                                "timestamp": "2025-07-25T01:17:50.466145500Z",
                                "code": "REVIEW_NOT_FOUND",
                                "message": "리뷰를 찾을 수 없습니다.",
                                "details": {
                                    "조회하려고 한 리뷰 아이디": "1364cc9c-ef00-4680-8e93-c8c72dc6d8a3"
                                },
                                "exceptionType": "ReviewNotFoundException",
                                "status": 404
                            }
                            """))
            ),
            @ApiResponse(
                    responseCode = "500", description = "서버 내부 오류",
                    content = @Content(examples = @ExampleObject(value = "리뷰 수정 중 서버 오류가 발생했습니다."))
            )
    })
    @PatchMapping("/{reviewId}")
    ResponseEntity<ReviewDto> updateReview(
            @Parameter(
                    name = "reviewId", description = "리뷰 ID", required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID reviewId,
            @Parameter(
                    description = "수정할 리뷰 데이터",
                    required = true)
            @Valid @RequestBody ReviewUpdateRequest updateRequest,
            @Parameter(
                    name = "Deokhugam-Request-User-ID", description = "요청자 ID", required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    );


    // 리뷰 논리 삭제
    @Operation(
            summary = "리뷰 논리 삭제",
            description = "본인이 작성한 리뷰를 논리적으로 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "리뷰 삭제 성공"),
            @ApiResponse(
                    responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                                "timestamp": "2025-07-25T01:20:27.596839300Z",
                                "code": "MISSING_HEADER",
                                "message": "필수 헤더가 누락되었습니다: Deokhugam-Request-User-ID",
                                "details": null,
                                "exceptionType": "MissingRequestHeaderException",
                                "status": 400
                            }
                            """))
            ),
            @ApiResponse(
                    responseCode = "403", description = "리뷰 삭제 권한 없음",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                                "timestamp": "2025-07-25T01:20:40.049809700Z",
                                "code": "REVIEW_NOT_OWNED",
                                "message": "본인이 작성한 리뷰만 수정/삭제할 수 있습니다.",
                                "details": {},
                                "exceptionType": "ReviewNotOwnedException",
                                "status": 403
                            }
                            """))
            ),
            @ApiResponse(
                    responseCode = "404", description = "리뷰 정보 없음",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                                "timestamp": "2025-07-25T01:20:57.487838100Z",
                                "code": "REVIEW_NOT_FOUND",
                                "message": "리뷰를 찾을 수 없습니다.",
                                "details": {
                                    "조회하려고 한 리뷰 아이디": "1364cc9c-ef00-4680-8e93-c8c72dc6d8a3"
                                },
                                "exceptionType": "ReviewNotFoundException",
                                "status": 404
                            }
                            """))
            ),
            @ApiResponse(
                    responseCode = "500", description = "서버 내부 오류",
                    content = @Content(examples = @ExampleObject(value = "리뷰 논리 삭제 중 서버 오류가 발생했습니다."))
            )
    })
    @DeleteMapping("/{reviewId}")
    ResponseEntity<Void> deleteReview(
            @Parameter(
                    name = "reviewId", description = "리뷰 ID", required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID reviewId,

            @Parameter(
                    name = "Deokhugam-Request-User-ID", description = "요청자 ID", required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    );

    // 리뷰 물리 삭제
    @Operation(
            summary = "리뷰 물리 삭제",
            description = "본인이 작성한 리뷰를 물리적으로 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "리뷰 삭제 성공"),
            @ApiResponse(
                    responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                                "timestamp": "2025-07-25T01:20:27.596839300Z",
                                "code": "MISSING_HEADER",
                                "message": "필수 헤더가 누락되었습니다: Deokhugam-Request-User-ID",
                                "details": null,
                                "exceptionType": "MissingRequestHeaderException",
                                "status": 400
                            }
                            """))
            ),
            @ApiResponse(
                    responseCode = "403", description = "리뷰 삭제 권한 없음",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                                "timestamp": "2025-07-25T01:20:40.049809700Z",
                                "code": "REVIEW_NOT_OWNED",
                                "message": "본인이 작성한 리뷰만 수정/삭제할 수 있습니다.",
                                "details": {},
                                "exceptionType": "ReviewNotOwnedException",
                                "status": 403
                            }
                            """))
            ),
            @ApiResponse(
                    responseCode = "404", description = "리뷰 정보 없음",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                                "timestamp": "2025-07-25T01:20:57.487838100Z",
                                "code": "REVIEW_NOT_FOUND",
                                "message": "리뷰를 찾을 수 없습니다.",
                                "details": {
                                    "조회하려고 한 리뷰 아이디": "1364cc9c-ef00-4680-8e93-c8c72dc6d8a3"
                                },
                                "exceptionType": "ReviewNotFoundException",
                                "status": 404
                            }
                            """))
            ),
            @ApiResponse(
                    responseCode = "500", description = "서버 내부 오류",
                    content = @Content(examples = @ExampleObject(value = "리뷰 물리 삭제 중 서버 오류가 발생했습니다."))
            )
    })
    @DeleteMapping("/{reviewId}/hard")
    ResponseEntity<Void> permanentDeleteReview(
            @Parameter(
                    name = "reviewId",
                    description = "리뷰 ID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID reviewId,

            @Parameter(
                    name = "Deokhugam-Request-User-ID",
                    description = "요청자 ID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    );

    // 리뷰 좋아요 스위치
    @Operation(
            summary = "리뷰 좋아요",
            description = "리뷰에 좋아요를 추가하거나 취소합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "리뷰 좋아요 성공",
                    content = @Content(schema = @Schema(implementation = ReviewLikeDto.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                                "timestamp": "2025-07-25T01:22:38.436546700Z",
                                "code": "MISSING_HEADER",
                                "message": "필수 헤더가 누락되었습니다: Deokhugam-Request-User-ID",
                                "details": null,
                                "exceptionType": "MissingRequestHeaderException",
                                "status": 400
                            }
                            """))
            ),
            @ApiResponse(
                    responseCode = "404", description = "리뷰 정보 없음",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                                "timestamp": "2025-07-25T01:22:55.860815600Z",
                                "code": "REVIEW_NOT_FOUND",
                                "message": "리뷰를 찾을 수 없습니다.",
                                "details": {
                                    "조회하려고 한 리뷰 아이디": "1364cc9c-ef00-4680-8e93-c8c72dc6d8a3"
                                },
                                "exceptionType": "ReviewNotFoundException",
                                "status": 404
                            }
                            """))
            ),
            @ApiResponse(
                    responseCode = "500", description = "서버 내부 오류",
                    content = @Content(examples = @ExampleObject(value = "리뷰 좋아요 처리 중 서버 오류가 발생했습니다."))
            )
    })
    @PostMapping("/{reviewId}/like")
    ResponseEntity<ReviewLikeDto> likeReview(
            @Parameter(
                    name = "reviewId", description = "리뷰 ID", required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID reviewId,

            @Parameter(
                    name = "Deokhugam-Request-User-ID", description = "요청자 ID", required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    );
}