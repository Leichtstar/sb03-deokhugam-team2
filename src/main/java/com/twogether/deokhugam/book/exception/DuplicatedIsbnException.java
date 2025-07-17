package com.twogether.deokhugam.book.exception;

import com.twogether.deokhugam.common.exception.ErrorCode;

public class DuplicatedIsbnException extends BookException {
	public DuplicatedIsbnException() {
		super(ErrorCode.DUPLICATED_ISBN);
	}

}
