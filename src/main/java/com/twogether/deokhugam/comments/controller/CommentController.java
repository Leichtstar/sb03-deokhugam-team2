package com.twogether.deokhugam.comments.controller;

import com.twogether.deokhugam.comments.dto.CommentCreateRequest;
import com.twogether.deokhugam.comments.dto.CommentResponse;
import com.twogether.deokhugam.comments.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 댓글 등록 API
 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "댓글 관리 API", description = "댓글 등록/조회/수정/삭제")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 등록", description = "리뷰에 댓글을 등록합니다.")
    @PostMapping
    public ResponseEntity<CommentResponse> create(@Valid @RequestBody CommentCreateRequest request) {
        CommentResponse response = commentService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}