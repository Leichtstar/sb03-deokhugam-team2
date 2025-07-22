package com.twogether.deokhugam.comments.exception;

public class CommentAlreadyDeletedException extends RuntimeException {

    public CommentAlreadyDeletedException(String message) {
        super(message);
    }

    public CommentAlreadyDeletedException() {
        super("이미 삭제된 댓글입니다.");
    }
}