package com.twogether.deokhugam.comments.controller;

import com.twogether.deokhugam.comments.dto.CommentCreateRequest;
import com.twogether.deokhugam.comments.dto.CommentResponse;
import com.twogether.deokhugam.comments.service.CommentQueryService;
import com.twogether.deokhugam.comments.service.CommentService;
import com.twogether.deokhugam.common.exception.dto.CursorPageResponse;
import jakarta.validation.Valid;
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
    @GetMapping
    public CursorPageResponse<CommentResponse> list(
        @RequestParam UUID reviewId,
        @RequestParam(defaultValue = "DESC") Direction direction,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime after,
        @RequestParam(required = false, defaultValue = "50") Integer limit) {

        return queryService.getComments(reviewId, direction, cursor, after, limit);
    }
}