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

    // Review 관련 에러 코드
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 작성한 리뷰가 있습니다."),
    INVALID_RATING(HttpStatus.BAD_REQUEST, "리뷰 평점은 1점 이상 5점 이하이어야 합니다."),
    REVIEW_LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰의 좋아요 정보를 찾을 수 없습니다."),
    REVIEW_NOT_OWNED(HttpStatus.FORBIDDEN, "본인이 작성한 리뷰만 수정/삭제할 수 있습니다."),

    // Batch 관련 에러 코드
    RANKING_DATA_EMPTY(HttpStatus.NOT_FOUND, "해당 기간의 리뷰 데이터가 존재하지 않습니다."),
    RANKING_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "인기 도서 랭킹 저장 중 오류가 발생했습니다."),

    // Dashboard 관련 에러 코드
    INVALID_RANKING_PERIOD(HttpStatus.BAD_REQUEST, "지원하지 않는 랭킹 기간입니다."),
    INVALID_DIRECTION(HttpStatus.BAD_REQUEST, "정렬 방향은 ASC 또는 DESC만 가능합니다."),

    // Cursor 관련 에러 코드
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "커서 정보가 올바르지 않습니다."),

    // Book 관련 에러 코드
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "등록되지 않은 도서입니다."),
    DUPLICATED_ISBN(HttpStatus.CONFLICT, "이미 사용된 ISBN 코드입니다."),
    INVALID_ISBN(HttpStatus.BAD_REQUEST, "잘못된 ISBN 코드입니다."),
    ISBN_NOT_FOUND(HttpStatus.NOT_FOUND, "미인증 ISBN 코드입니다."),
    NAVER_API_CONNECTION_FAILED(HttpStatus.BAD_GATEWAY, "네이버 API 서버에 연결할 수 없습니다."),
    NAVER_API_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "네이버 API 인증에 실패했습니다."),
    NAVER_API_THUMBNAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."),

    // Comment 관련 에러 코드
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다.");


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
