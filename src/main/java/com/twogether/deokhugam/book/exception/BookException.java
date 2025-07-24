package com.twogether.deokhugam.book.exception;

import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;

public class BookException extends DeokhugamException {

	public BookException(ErrorCode errorCode) {
		super(errorCode);
	}

	public BookException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}

}
