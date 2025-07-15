package com.twogether.deokhugam.user.exception;

import com.twogether.deokhugam.common.exception.ErrorCode;

public class EmailAlreadyExistsException extends UserException {
    public EmailAlreadyExistsException() {
        super(ErrorCode.EMAIL_DUPLICATION);
    }

    public static EmailAlreadyExistsException withEmail(String email) {
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException();
        exception.addDetail("email", email);
        return exception;
    }
}
