package com.twogether.deokhugam.review.controller;

import com.twogether.deokhugam.review.dto.ReviewDto;
import com.twogether.deokhugam.review.dto.ReviewLikeDto;
import com.twogether.deokhugam.review.dto.request.ReviewCreateRequest;
import com.twogether.deokhugam.review.exception.ReviewNotFoundException;
import com.twogether.deokhugam.review.service.ReviewService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RequestMapping("/api/reviews")
@RestController
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 생성
    @PostMapping
    public ResponseEntity<ReviewDto> create(
            @Valid @RequestBody ReviewCreateRequest request
    ){
        ReviewDto reviewDto = reviewService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewDto);
    }

    // 리뷰 상세 조회
    @GetMapping(path = "/{reviewId}")
    public ResponseEntity<ReviewDto> findById(
            @PathVariable("reviewId") UUID reviewId,
            @RequestHeader(value = "Deokhugam-Request-User-ID", required = true) UUID requestUserId
    ){
        ReviewDto reviewDto = reviewService.findById(reviewId, requestUserId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reviewDto);
    }

    // 리뷰 좋아요 스위치 요청
    @PostMapping("/{reviewId}/like")
    public ResponseEntity<ReviewLikeDto> reviewLike(
            @PathVariable("reviewId") UUID reviewId,
            @RequestHeader(value = "Deokhugam-Request-User-ID", required = true) UUID requestUserId
    ){
        ReviewLikeDto reviewLikeDto = reviewService.reviewLike(reviewId, requestUserId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reviewLikeDto);
    }
}