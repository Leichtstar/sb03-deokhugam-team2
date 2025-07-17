package com.twogether.deokhugam.review.exception;

import com.twogether.deokhugam.common.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class ReviewNotFoundException extends ReviewException {

    public ReviewNotFoundException(UUID reviewId) {
        super(ErrorCode.REVIEW_NOT_FOUND, Map.of("조회하려고 한 리뷰 아이디", reviewId));
    }
}
