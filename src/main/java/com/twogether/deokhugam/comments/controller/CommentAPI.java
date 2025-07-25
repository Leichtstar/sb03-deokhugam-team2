package com.twogether.deokhugam.comments.controller;

import com.twogether.deokhugam.comments.dto.CommentCreateRequest;
import com.twogether.deokhugam.comments.dto.CommentResponse;
import com.twogether.deokhugam.comments.dto.CommentUpdateRequest;
import com.twogether.deokhugam.common.dto.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@Tag(name = "댓글 관리 API", description = "댓글 등록/조회/수정/삭제")
@RequestMapping("/api/comments")
public interface CommentAPI {

    @Operation(summary = "댓글 등록", description = "리뷰에 댓글을 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "댓글 등록 성공",
            content = @Content(schema = @Schema(implementation = CommentResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
            content = @Content(examples = @ExampleObject(value = """
{
  "timestamp": "2025-07-25T12:05:34.294Z",
  "code": "INVALID_INPUT_VALUE",
  "message": "잘못된 입력값입니다.",
  "details": {
    "content": "댓글 내용은 1~200자 이내여야 합니다."
  },
  "exceptionType": "MethodArgumentNotValidException",
  "status": 400
}
"""))),
        @ApiResponse(responseCode = "404", description = "리뷰 정보 없음",
            content = @Content(examples = @ExampleObject(value = """
{
  "timestamp": "2025-07-25T12:08:52.777Z",
  "code": "REVIEW_NOT_FOUND",
  "message": "리뷰를 찾을 수 없습니다.",
  "details": {},
  "exceptionType": "ReviewNotFoundException",
  "status": 404
}
"""))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(examples = @ExampleObject(value = "댓글 등록 중 서버 오류가 발생했습니다.")))
    })
    @PostMapping
    ResponseEntity<CommentResponse> create(
        @Parameter(description = "댓글 생성 요청 데이터") @Valid @RequestBody CommentCreateRequest request
    );

    @Operation(summary = "댓글 목록 조회", description = "시간 역순 + 커서 페이지네이션")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = CursorPageResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 방향 오류, 페이징 파라미터 오류, 리뷰 ID 누락)",
            content = @Content(examples = @ExampleObject(value = """
{
  "timestamp": "2025-07-25T12:14:55.436Z",
  "code": "MISSING_PARAMETER",
  "message": "리뷰 ID는 필수입니다.",
  "details": null,
  "exceptionType": "MissingRequestParameterException",
  "status": 400
}
"""))),
        @ApiResponse(responseCode = "404", description = "리뷰 정보 없음",
            content = @Content(examples = @ExampleObject(value = """
{
  "timestamp": "2025-07-25T12:13:30.906Z",
  "code": "REVIEW_NOT_FOUND",
  "message": "리뷰를 찾을 수 없습니다.",
  "details": {
    "reviewId": "1364cc9c-ef00-4680-8e93-c8c72dc6d8a3"
  },
  "exceptionType": "ReviewNotFoundException",
  "status": 404
}
"""))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(examples = @ExampleObject(value = "댓글 목록 조회 중 서버 오류가 발생했습니다.")))
    })
    @GetMapping
    CursorPageResponse<CommentResponse> list(
        @Parameter(description = "리뷰 ID", required = true)
        @RequestParam UUID reviewId,
        @Parameter(description = "정렬 방향", example = "DESC", schema = @Schema(allowableValues = {"ASC", "DESC"}))
        @RequestParam(defaultValue = "DESC") Direction direction,
        @Parameter(description = "커서(댓글 ID)")
        @RequestParam(required = false) String cursor,
        @Parameter(description = "after 기준 Instant(UTC, ISO 8601)")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
        @Parameter(description = "페이지 크기", example = "50")
        @RequestParam(defaultValue = "50") @Min(1) @Max(100) Integer limit
    );

    @Operation(summary = "댓글 상세 조회", description = "댓글 ID로 댓글의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "댓글 상세 조회 성공",
            content = @Content(schema = @Schema(implementation = CommentResponse.class))),
        @ApiResponse(responseCode = "404", description = "댓글 정보 없음",
            content = @Content(examples = @ExampleObject(value = """
{
  "timestamp": "2025-07-25T12:13:30.906Z",
  "code": "COMMENT_NOT_FOUND",
  "message": "댓글을 찾을 수 없습니다.",
  "details": {
    "commentId": "3f0cce7b-85c5-441a-bc45-8dd726feef44"
  },
  "exceptionType": "CommentNotFoundException",
  "status": 404
}
"""))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(examples = @ExampleObject(value = "댓글 상세 조회 중 서버 오류가 발생했습니다.")))
    })
    @GetMapping("/{commentId}")
    ResponseEntity<CommentResponse> get(
        @Parameter(description = "댓글 ID", required = true) @PathVariable UUID commentId
    );

    @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "댓글 수정 성공",
            content = @Content(schema = @Schema(implementation = CommentResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
            content = @Content(examples = @ExampleObject(value = """
{
  "timestamp": "2025-07-25T12:18:43.259Z",
  "code": "INVALID_INPUT_VALUE",
  "message": "잘못된 입력값입니다.",
  "details": {
    "content": "댓글 내용은 1~200자 이내여야 합니다."
  },
  "exceptionType": "MethodArgumentNotValidException",
  "status": 400
}
"""))),
        @ApiResponse(responseCode = "403", description = "댓글 수정 권한 없음",
            content = @Content(examples = @ExampleObject(value = """
{
  "timestamp": "2025-07-25T12:18:15.235Z",
  "code": "COMMENT_NOT_OWNED",
  "message": "본인이 작성한 댓글만 수정할 수 있습니다.",
  "details": {},
  "exceptionType": "CommentNotOwnedException",
  "status": 403
}
"""))),
        @ApiResponse(responseCode = "404", description = "댓글 정보 없음",
            content = @Content(examples = @ExampleObject(value = """
{
  "timestamp": "2025-07-25T12:17:50.466Z",
  "code": "COMMENT_NOT_FOUND",
  "message": "댓글을 찾을 수 없습니다.",
  "details": {
    "commentId": "3f0cce7b-85c5-441a-bc45-8dd726feef44"
  },
  "exceptionType": "CommentNotFoundException",
  "status": 404
}
"""))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(examples = @ExampleObject(value = "댓글 수정 중 서버 오류가 발생했습니다.")))
    })
    @PatchMapping("/{commentId}")
    ResponseEntity<CommentResponse> update(
        @Parameter(description = "댓글 ID", required = true) @PathVariable UUID commentId,
        @Parameter(description = "요청자 ID", required = true) @RequestHeader("Deokhugam-Request-User-ID") UUID userId,
        @Parameter(description = "댓글 수정 요청 데이터", required = true) @Valid @RequestBody CommentUpdateRequest request
    );

    @Operation(summary = "댓글 논리 삭제", description = "본인이 작성한 댓글을 논리적으로 삭제합니다. (isDeleted = true)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "댓글 논리 삭제 성공"),
        @ApiResponse(responseCode = "403", description = "댓글 삭제 권한 없음",
            content = @Content(examples = @ExampleObject(value = """
{
  "timestamp": "2025-07-25T12:20:40.049Z",
  "code": "COMMENT_NOT_OWNED",
  "message": "본인이 작성한 댓글만 삭제할 수 있습니다.",
  "details": {},
  "exceptionType": "CommentNotOwnedException",
  "status": 403
}
"""))),
        @ApiResponse(responseCode = "404", description = "댓글 정보 없음",
            content = @Content(examples = @ExampleObject(value = """
{
  "timestamp": "2025-07-25T12:20:57.487Z",
  "code": "COMMENT_NOT_FOUND",
  "message": "댓글을 찾을 수 없습니다.",
  "details": {
    "commentId": "3f0cce7b-85c5-441a-bc45-8dd726feef44"
  },
  "exceptionType": "CommentNotFoundException",
  "status": 404
}
"""))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(examples = @ExampleObject(value = "댓글 논리 삭제 중 서버 오류가 발생했습니다.")))
    })
    @DeleteMapping("/{commentId}")
    ResponseEntity<Void> logicalDelete(
        @Parameter(description = "댓글 ID", required = true) @PathVariable UUID commentId,
        @Parameter(description = "요청자 ID", required = true) @RequestHeader("Deokhugam-Request-User-ID") UUID userId
    );

    @Operation(summary = "댓글 물리 삭제", description = "본인이 작성한 댓글을 DB에서 영구적으로 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "댓글 물리 삭제 성공"),
        @ApiResponse(responseCode = "403", description = "댓글 삭제 권한 없음",
            content = @Content(examples = @ExampleObject(value = """
{
  "timestamp": "2025-07-25T12:20:40.049Z",
  "code": "COMMENT_NOT_OWNED",
  "message": "본인이 작성한 댓글만 삭제할 수 있습니다.",
  "details": {},
  "exceptionType": "CommentNotOwnedException",
  "status": 403
}
"""))),
        @ApiResponse(responseCode = "404", description = "댓글 정보 없음",
            content = @Content(examples = @ExampleObject(value = """
{
  "timestamp": "2025-07-25T12:20:57.487Z",
  "code": "COMMENT_NOT_FOUND",
  "message": "댓글을 찾을 수 없습니다.",
  "details": {
    "commentId": "3f0cce7b-85c5-441a-bc45-8dd726feef44"
  },
  "exceptionType": "CommentNotFoundException",
  "status": 404
}
"""))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(examples = @ExampleObject(value = "댓글 물리 삭제 중 서버 오류가 발생했습니다.")))
    })
    @DeleteMapping("/{commentId}/hard")
    ResponseEntity<Void> physicalDelete(
        @Parameter(description = "댓글 ID", required = true) @PathVariable UUID commentId,
        @Parameter(description = "요청자 ID", required = true) @RequestHeader("Deokhugam-Request-User-ID") UUID userId
    );
}
