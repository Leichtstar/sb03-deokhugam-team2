package com.twogether.deokhugam.review.exception;

import com.twogether.deokhugam.common.exception.ErrorCode;

public class ReviewLikeNotFoundException extends ReviewException {

    public ReviewLikeNotFoundException() {
        super(ErrorCode.REVIEW_LIKE_NOT_FOUND);
    }
}
