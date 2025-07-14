package com.twogether.deokhugam.user.exception;

import com.twogether.deokhugam.common.exception.ErrorCode;

public class UserAlreadyExistsException extends UserException {
    public UserAlreadyExistsException() {
        super(ErrorCode.DUPLICATE_USER);
    }

    public static UserAlreadyExistsException withEmail(String email) {
        UserAlreadyExistsException exception = new UserAlreadyExistsException();
        exception.addDetail("email", email);
        return exception;
    }

    public static UserAlreadyExistsException withNickname(String nickname) {
        UserAlreadyExistsException exception = new UserAlreadyExistsException();
        exception.addDetail("nickname", nickname);
        return exception;
    }
}
