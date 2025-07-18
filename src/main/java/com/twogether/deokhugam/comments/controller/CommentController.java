package com.twogether.deokhugam.comments.controller;

import com.twogether.deokhugam.comments.dto.CommentCreateRequest;
import com.twogether.deokhugam.comments.dto.CommentResponse;
import com.twogether.deokhugam.comments.dto.CommentUpdateRequest;
import com.twogether.deokhugam.comments.service.CommentQueryService;
import com.twogether.deokhugam.comments.service.CommentService;
import com.twogether.deokhugam.common.dto.CursorPageResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Sort.Direction;

/**
 * 댓글 등록 API
 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "댓글 관리 API", description = "댓글 등록/조회/수정/삭제")
public class CommentController {

    private final CommentService commentService;
    private final CommentQueryService queryService;

    @Operation(summary = "댓글 등록", description = "리뷰에 댓글을 등록합니다.")
    @PostMapping
    public ResponseEntity<CommentResponse> create(@Valid @RequestBody CommentCreateRequest request) {
        CommentResponse response = commentService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "댓글 목록 조회", description = "시간 역순 + 커서 페이지네이션")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "댓글 목록 조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CursorPageResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (정렬 방향 오류, 페이징 파라미터 오류, 리뷰 ID 누락)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CursorPageResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "리뷰 정보 없음",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CursorPageResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CursorPageResponse.class))
        )
    })
    @GetMapping
    public CursorPageResponse<CommentResponse> list(
        @RequestParam UUID reviewId,
        @RequestParam(defaultValue = "DESC") Direction direction,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
        @RequestParam(required = false, defaultValue = "50") @Min(1) @Max(100) Integer limit
    ) {
        return queryService.getComments(reviewId, direction, cursor, after, limit);
    }

    @Operation(summary = "댓글 상세 조회", description = "댓글 ID로 댓글의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
                @ApiResponse(
                    responseCode = "200",
                    description = "댓글 상세 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponse.class))
        ),
            @ApiResponse(
                    responseCode = "404",
                    description = "댓글을 찾을 수 없음",
                    content = @Content(mediaType = "application/json")
        )
        })
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> get(@PathVariable UUID commentId) {
        return ResponseEntity.ok(commentService.getComment(commentId));
    }

    @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글을 수정합니다.")
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> update(
        @PathVariable UUID commentId,
        @RequestHeader("Deokhugam-Request-User-Id") UUID userId,
        @Valid @RequestBody CommentUpdateRequest request
    ) {
        return ResponseEntity.ok(commentService.updateComment(commentId, userId, request));
    }

    @Operation(
        summary = "댓글 논리 삭제",
        description = "본인이 작성한 댓글을 논리적으로 삭제합니다. <br>DB에는 남아 있고, isDeleted가 true로 변경되어 더 이상 조회되지 않습니다."
    )
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> logicalDelete(
        @PathVariable UUID commentId,
        @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    ) {
        commentService.deleteLogical(commentId, requestUserId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "댓글 물리 삭제",
        description = "본인이 작성한 댓글을 DB에서 영구적으로 삭제합니다. <br>삭제된 댓글은 복구할 수 없습니다."
    )
    @DeleteMapping("/{commentId}/hard")
    public ResponseEntity<Void> physicalDelete(
        @PathVariable UUID commentId,
        @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    ) {
        commentService.deletePhysical(commentId, requestUserId);
        return ResponseEntity.noContent().build();
    }

}