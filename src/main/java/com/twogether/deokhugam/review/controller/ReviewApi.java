package com.twogether.deokhugam.review.controller;

import com.twogether.deokhugam.common.dto.CursorPageResponseDto;
import com.twogether.deokhugam.review.dto.ReviewDto;
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
                    content = @Content(examples = @ExampleObject(value = "입력값 검증에 실패했습니다."))
            ),
            @ApiResponse(
                    responseCode = "404", description = "도서 정보 없음",
                    content = @Content(examples = @ExampleObject(value = "리뷰를 작성하고자 하는 도서가 존재하지 않습니다. {request.bookId}"))
            ),
            @ApiResponse(
                    responseCode = "409", description = "이미 작성된 리뷰 존재",
                    content = @Content(examples = @ExampleObject(value = "이미 작성한 리뷰가 있습니다."))
            ),
            @ApiResponse(
                    responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ReviewDto.class))
            )}

    )
    @PostMapping
    ResponseEntity<ReviewDto> createReview(
            @Parameter(description = "리뷰 생성 정보")
            ReviewCreateRequest request
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
                    content = @Content(examples = @ExampleObject(value = "리뷰를 조회하고자 하는 사용자의 Id가 누락됐습니다."))
            ),
            @ApiResponse(
                    responseCode = "404", description = "리뷰 정보 없음",
                    content = @Content(examples = @ExampleObject(value = "조회하고자 하는 리뷰가 존재하지 않습니다."))
            ),
            @ApiResponse(
                    responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ReviewDto.class))
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
                    content = @Content(examples = @ExampleObject(value = "orderBy 값이 유효하지 않거나, after 또는 cursor 형식이 잘못됐습니다."))
            ),
            @ApiResponse(
                    responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = CursorPageResponseDto.class))
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
                    example = "createdAt"
            )
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @Parameter(
                    name = "direction", description = "정렬 방향",
                    example = "DESC",
                    schema = @Schema(allowableValues = {"ASC", "DESC"})
            )
            @RequestParam(defaultValue = "DESC") String direction,
            @Parameter(
                    name = "cursor",
                    description = "커서 페이지네이션 커서",
                    required = false
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
                    content = @Content(examples = @ExampleObject(value = "입력값 검증에 실패했습니다."))
            ),
            @ApiResponse(
                    responseCode = "403", description = "리뷰 수정 권한 없음",
                    content = @Content(examples = @ExampleObject(value = "타인이 작성한 리뷰는 수정할 수 없습니다."))
            ),
            @ApiResponse(
                    responseCode = "404", description = "리뷰 정보 없음",
                    content = @Content(examples = @ExampleObject(value = "수정하고자 하는 리뷰가 존재하지 않습니다."))
            ),
            @ApiResponse(
                    responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ReviewDto.class))
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
                    content = @Content(examples = @ExampleObject(value = "리뷰를 삭제하려면 요청자의 ID가 필요합니다."))
            ),
            @ApiResponse(
                    responseCode = "403", description = "리뷰 삭제 권한 없음",
                    content = @Content(examples = @ExampleObject(value = "해당 리뷰에 대한 삭제 권한이 없습니다."))
            ),
            @ApiResponse(
                    responseCode = "404", description = "리뷰 정보 없음",
                    content = @Content(examples = @ExampleObject(value = "삭제하려는 리뷰가 존재하지 않습니다."))
            ),
            @ApiResponse(
                    responseCode = "500", description = "서버 내부 오류",
                    content = @Content(examples = @ExampleObject(value = "리뷰 삭제 중 서버 오류가 발생했습니다."))
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
                    content = @Content(examples = @ExampleObject(value = "리뷰를 삭제하려면 요청자의 ID가 필요합니다."))
            ),
            @ApiResponse(
                    responseCode = "403", description = "리뷰 삭제 권한 없음",
                    content = @Content(examples = @ExampleObject(value = "해당 리뷰에 대한 삭제 권한이 없습니다."))
            ),
            @ApiResponse(
                    responseCode = "404", description = "리뷰 정보 없음",
                    content = @Content(examples = @ExampleObject(value = "삭제하려는 리뷰가 존재하지 않습니다."))
            ),
            @ApiResponse(
                    responseCode = "500", description = "서버 내부 오류",
                    content = @Content(examples = @ExampleObject(value = "리뷰 삭제 중 서버 오류가 발생했습니다."))
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
}