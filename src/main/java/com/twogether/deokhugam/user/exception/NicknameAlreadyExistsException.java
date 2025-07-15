package com.twogether.deokhugam.user.exception;

import com.twogether.deokhugam.common.exception.ErrorCode;

public class NicknameAlreadyExistsException extends UserException {
    public NicknameAlreadyExistsException() {
        super(ErrorCode.NICKNAME_DUPLICATION);
    }

    public static NicknameAlreadyExistsException withNickname(String nickname) {
        NicknameAlreadyExistsException exception = new NicknameAlreadyExistsException();
        exception.addDetail("nickname", nickname);
        return exception;
    }
}
