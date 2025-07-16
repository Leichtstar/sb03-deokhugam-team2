package com.twogether.deokhugam.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // User 관련 에러 코드
    EMAIL_DUPLICATION(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    NICKNAME_DUPLICATION(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    LOGIN_INPUT_INVALID(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "사용자 정보 수정 권한이 없습니다."),

    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 작성한 리뷰가 있습니다."),
    INVALID_RATING(HttpStatus.BAD_REQUEST, "리뷰 평점은 1점 이상 5점 이하이어야 합니다."),

    // Dashboard 관련 에러 코드
    INVALID_RANKING_PERIOD(HttpStatus.BAD_REQUEST, "지원하지 않는 랭킹 기간입니다."),
    INVALID_DIRECTION(HttpStatus.BAD_REQUEST, "정렬 방향은 ASC 또는 DESC만 가능합니다.");

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
