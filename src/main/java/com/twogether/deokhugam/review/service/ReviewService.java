package com.twogether.deokhugam.review.service;

import com.twogether.deokhugam.review.dto.ReviewDto;
import com.twogether.deokhugam.review.dto.request.ReviewCreateRequest;

public interface ReviewService {

    // 리뷰 생성
    ReviewDto create(ReviewCreateRequest request);


}
