package com.twogether.deokhugam.review.service;

import com.twogether.deokhugam.review.dto.ReviewDto;
import com.twogether.deokhugam.review.dto.ReviewLikeDto;
import com.twogether.deokhugam.review.dto.request.ReviewCreateRequest;
import java.util.UUID;

public interface ReviewService {

    // 리뷰 생성
    ReviewDto create(ReviewCreateRequest request);

    // 리뷰 상세 정보 조회
    ReviewDto findById(UUID reviewId, UUID requestUserId);

    // 리뷰 좋아요 기능
    ReviewLikeDto reviewLike(UUID reviewId, UUID userId);

}
