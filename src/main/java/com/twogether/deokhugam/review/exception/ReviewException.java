package com.twogether.deokhugam.review.exception;

import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import java.util.Map;

public abstract class ReviewException extends DeokhugamException {

    protected ReviewException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected ReviewException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
