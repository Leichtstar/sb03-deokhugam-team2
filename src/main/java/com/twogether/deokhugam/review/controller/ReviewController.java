package com.twogether.deokhugam.review.controller;

import com.twogether.deokhugam.common.dto.CursorPageResponseDto;
import com.twogether.deokhugam.review.dto.ReviewDto;
import com.twogether.deokhugam.review.dto.ReviewLikeDto;
import com.twogether.deokhugam.review.dto.request.ReviewCreateRequest;
import com.twogether.deokhugam.review.dto.request.ReviewSearchRequest;
import com.twogether.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.twogether.deokhugam.review.service.ReviewService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RequestMapping("/api/reviews")
@RestController
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 생성
    @PostMapping
    public ResponseEntity<ReviewDto> createReview(
            @Valid @RequestBody ReviewCreateRequest request
    ){
        ReviewDto reviewDto = reviewService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewDto);
    }

    // 리뷰 상세 조회
    @GetMapping(path = "/{reviewId}")
    public ResponseEntity<ReviewDto> getReview(
            @PathVariable("reviewId") UUID reviewId,
            @RequestHeader(value = "Deokhugam-Request-User-ID", required = true) UUID requestUserId
    ){
        ReviewDto reviewDto = reviewService.findById(reviewId, requestUserId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reviewDto);
    }

    // 리뷰 목록 조회
    @GetMapping
    public ResponseEntity<CursorPageResponseDto<ReviewDto>> searchReviews(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID bookId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String after,
            @RequestParam(defaultValue = "50") int limit,
            @RequestHeader(value = "Deokhugam-Request-User-ID", required = true) UUID requestUserId
    ){
        ReviewSearchRequest request = new ReviewSearchRequest(
                userId, bookId, keyword, orderBy, direction, cursor, after, limit, requestUserId
        );

        CursorPageResponseDto<ReviewDto> searchResult = reviewService.findReviews(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(searchResult);
    }

    // 리뷰 수정
    @PatchMapping(path = "/{reviewId}")
    public ResponseEntity<ReviewDto> updateReview(
            @PathVariable("reviewId") UUID reviewId,
            @Valid @RequestBody ReviewUpdateRequest updateRequest,
            @RequestHeader(value = "Deokhugam-Request-User-ID", required = true) UUID requestUserId
    ){
        ReviewDto reviewDto = reviewService.updateReview(reviewId, requestUserId, updateRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reviewDto);
    }

    // 리뷰 좋아요 스위치 요청
    @PostMapping("/{reviewId}/like")
    public ResponseEntity<ReviewLikeDto> likeReview(
            @PathVariable("reviewId") UUID reviewId,
            @RequestHeader(value = "Deokhugam-Request-User-ID", required = true) UUID requestUserId
    ){
        ReviewLikeDto reviewLikeDto = reviewService.reviewLike(reviewId, requestUserId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reviewLikeDto);
    }
}