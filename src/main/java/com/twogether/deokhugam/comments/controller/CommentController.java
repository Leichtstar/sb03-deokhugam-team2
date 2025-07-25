package com.twogether.deokhugam.comments.controller;

import com.twogether.deokhugam.comments.dto.CommentCreateRequest;
import com.twogether.deokhugam.comments.dto.CommentResponse;
import com.twogether.deokhugam.comments.dto.CommentUpdateRequest;
import com.twogether.deokhugam.comments.service.CommentQueryService;
import com.twogether.deokhugam.comments.service.CommentService;
import com.twogether.deokhugam.common.dto.CursorPageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort.Direction;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController implements CommentAPI {

    private final CommentService commentService;
    private final CommentQueryService queryService;

    @Override
    public ResponseEntity<CommentResponse> create(@Valid @RequestBody CommentCreateRequest request) {
        CommentResponse response = commentService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public CursorPageResponse<CommentResponse> list(
        @RequestParam UUID reviewId,
        @RequestParam(defaultValue = "DESC") Direction direction,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
        @RequestParam(required = false, defaultValue = "50") @Min(1) @Max(100) Integer limit
    ) {
        return queryService.getComments(reviewId, direction, cursor, after, limit);
    }

    @Override
    public ResponseEntity<CommentResponse> get(@PathVariable UUID commentId) {
        return ResponseEntity.ok(commentService.getComment(commentId));
    }

    @Override
    public ResponseEntity<CommentResponse> update(
        @PathVariable UUID commentId,
        @RequestHeader("Deokhugam-Request-User-ID") UUID userId,
        @Valid @RequestBody CommentUpdateRequest request
    ) {
        return ResponseEntity.ok(commentService.updateComment(commentId, userId, request));
    }

    @Override
    public ResponseEntity<Void> logicalDelete(
        @PathVariable UUID commentId,
        @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    ) {
        commentService.deleteLogical(commentId, requestUserId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> physicalDelete(
        @PathVariable UUID commentId,
        @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    ) {
        commentService.deletePhysical(commentId, requestUserId);
        return ResponseEntity.noContent().build();
    }
}
