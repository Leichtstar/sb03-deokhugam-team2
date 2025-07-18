package com.twogether.deokhugam.review.exception;

import com.twogether.deokhugam.common.exception.ErrorCode;

public class ReviewNotOwnedException extends ReviewException {

    public ReviewNotOwnedException() {
        super(ErrorCode.REVIEW_NOT_OWNED);
    }
}
