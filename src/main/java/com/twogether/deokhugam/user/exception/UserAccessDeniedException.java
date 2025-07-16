package com.twogether.deokhugam.user.exception;

import com.twogether.deokhugam.common.exception.ErrorCode;
import java.util.UUID;

public class UserAccessDeniedException extends UserException {
    public UserAccessDeniedException() {
        super(ErrorCode.USER_ACCESS_DENIED);
    }

    public static UserAccessDeniedException userIdMismatch(UUID requestUserId, UUID targetUserId) {
        UserAccessDeniedException exception = new UserAccessDeniedException();
        exception.addDetail("requestUserId", requestUserId.toString());
        exception.addDetail("targetUserId", targetUserId.toString());
        return exception;
    }

    public static UserAccessDeniedException missingUserIdHeader() {
        UserAccessDeniedException exception = new UserAccessDeniedException();
        exception.addDetail("missingUserIdHeader", "");
        return exception;
    }

    public static UserAccessDeniedException invalidUserIdFormat(String invalidUserId) {
        UserAccessDeniedException exception = new UserAccessDeniedException();
        exception.addDetail("invalidUserId", invalidUserId);
        return exception;
    }
}
