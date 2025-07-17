package com.twogether.deokhugam.comments.exception;

import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;

public class CommentNotFoundException extends DeokhugamException {
    public CommentNotFoundException() {
        super(ErrorCode.COMMENT_NOT_FOUND); // (아래 설명 참고)
    }
}