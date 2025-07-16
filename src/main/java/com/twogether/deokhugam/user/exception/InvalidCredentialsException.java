package com.twogether.deokhugam.user.exception;

import com.twogether.deokhugam.common.exception.ErrorCode;

public class InvalidCredentialsException extends UserException {
    public InvalidCredentialsException() {
        super(ErrorCode.LOGIN_INPUT_INVALID);
    }

    public static InvalidCredentialsException wrongPassword() {
        InvalidCredentialsException exception = new InvalidCredentialsException();
        return exception;
    }

    public static InvalidCredentialsException emailNotFound() {
        InvalidCredentialsException exception = new InvalidCredentialsException();
        return exception;
    }
} 