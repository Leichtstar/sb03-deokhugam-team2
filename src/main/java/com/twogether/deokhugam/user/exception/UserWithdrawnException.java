package com.twogether.deokhugam.user.exception;

import com.twogether.deokhugam.common.exception.ErrorCode;

public class UserWithdrawnException extends UserException {
    public UserWithdrawnException() {
        super(ErrorCode.USER_WITHDRAWN);
    }

    public static UserWithdrawnException withEmail(String email) {
        UserWithdrawnException exception = new UserWithdrawnException();
        exception.addDetail("email", email);
        return exception;
    }
}
