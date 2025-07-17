package com.twogether.deokhugam.book.exception;

import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;

public class NaverBookException extends DeokhugamException {
	public NaverBookException(ErrorCode errorCode) {
		super(errorCode);
	}
	public NaverBookException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}
