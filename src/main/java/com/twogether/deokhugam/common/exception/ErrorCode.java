package com.twogether.deokhugam.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 작성한 리뷰가 있습니다."),
    INVALID_RATING(HttpStatus.BAD_REQUEST, "리뷰 평점은 1점 이상 5점 이하이어야 합니다.");

    private final String message;
    private final HttpStatus status;

    ErrorCode(HttpStatus status, String message){
        this.status = status;
        this.message = message;
    }

    // HTTP 상태 코드 숫자값 반환
    public int getStatus() {
        return this.status.value();
    }

    // HttpStatus 객체를 반환
    public HttpStatus getHttpStatus() {
        return this.status;
    }
}
