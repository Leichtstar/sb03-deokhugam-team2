package com.twogether.deokhugam.review.exception;

import com.twogether.deokhugam.common.exception.ErrorCode;
import java.util.UUID;

public class ReviewExistException extends ReviewException {

    public ReviewExistException(UUID userId, UUID bookId) {
        super(ErrorCode.REVIEW_ALREADY_EXISTS);
    }
}
