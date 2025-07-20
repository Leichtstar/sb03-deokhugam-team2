package com.twogether.deokhugam.comments.exception;

import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;

public class CommentForbiddenException extends DeokhugamException {
    public CommentForbiddenException() {
        super(ErrorCode.COMMENT_FORBIDDEN);
    }
}
