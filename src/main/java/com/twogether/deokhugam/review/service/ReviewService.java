package com.twogether.deokhugam.review.service;

import com.twogether.deokhugam.common.dto.CursorPageResponseDto;
import com.twogether.deokhugam.review.dto.ReviewDto;
import com.twogether.deokhugam.review.dto.ReviewLikeDto;
import com.twogether.deokhugam.review.dto.request.ReviewCreateRequest;
import com.twogether.deokhugam.review.dto.request.ReviewSearchRequest;
import com.twogether.deokhugam.review.dto.request.ReviewUpdateRequest;
import java.util.UUID;

public interface ReviewService {

    // 리뷰 생성
    ReviewDto create(ReviewCreateRequest request);

    // 리뷰 상세 정보 조회
    ReviewDto findById(UUID reviewId, UUID requestUserId);

    // 리뷰 목록 조회
    CursorPageResponseDto<ReviewDto> findReviews(ReviewSearchRequest keyword);

    // 리뷰 수정
    ReviewDto updateReview(UUID reviewId, UUID requestUserId, ReviewUpdateRequest updateRequest);

    // 리뷰 논리 삭제
    void deleteReviewSoft(UUID reviewId, UUID requestUserId);

    // 리뷰 좋아요 기능
    ReviewLikeDto reviewLike(UUID reviewId, UUID userId);
}