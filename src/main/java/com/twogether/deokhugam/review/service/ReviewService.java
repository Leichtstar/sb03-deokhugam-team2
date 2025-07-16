package com.twogether.deokhugam.review.service;

import com.twogether.deokhugam.review.dto.ReviewDto;
import com.twogether.deokhugam.review.dto.ReviewLikeDto;
import com.twogether.deokhugam.review.dto.request.ReviewCreateRequest;
import com.twogether.deokhugam.review.dto.request.ReviewSearchRequest;
import com.twogether.deokhugam.review.entity.Review;
import java.util.List;
import java.util.UUID;

public interface ReviewService {

    // 리뷰 생성
    ReviewDto create(ReviewCreateRequest request);

    // 리뷰 상세 정보 조회
    ReviewDto findById(UUID reviewId, UUID requestUserId);

    // 리뷰 좋아요 기능
    ReviewLikeDto reviewLike(UUID reviewId, UUID userId);

    // 리뷰 목록 조회
    List<Review> findReviews(ReviewSearchRequest keyword);
}
