package com.twogether.deokhugam.book.exception;

import com.twogether.deokhugam.common.exception.ErrorCode;
import java.util.UUID;

public class BookNotFoundException extends BookException {
	public BookNotFoundException() {
		super(ErrorCode.BOOK_NOT_FOUND);
	}

}
