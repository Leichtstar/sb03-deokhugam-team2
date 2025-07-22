package com.twogether.deokhugam.common.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseBody
  public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body("잘못된 요청입니다.: " + ex.getMessage());
  }

  @ExceptionHandler(NoSuchElementException.class)
  @ResponseBody
  public ResponseEntity<String> handleNoSuchElement(NoSuchElementException ex) {
    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body("리소스를 찾을 수 없습니다.: " + ex.getMessage());
  }

  @ExceptionHandler(NullPointerException.class)
  @ResponseBody
  public ResponseEntity<String> handleNullPointer(NullPointerException ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("널포인터 예외가 발생했습니다.: " + ex.getMessage());
  }

  // 커스텀 예외 처리
  @ExceptionHandler(DeokhugamException.class)
  @ResponseBody
  public ResponseEntity<ErrorResponse> handleDeokhugamException(DeokhugamException e){
    log.warn("[GlobalExceptionHandler] DeokhugamException 발생: {}", e.getMessage(), e);

    int status = e.getErrorCode().getStatus();
    ErrorResponse errorResponse = ErrorResponse.of(e);

    return ResponseEntity
            .status(status)
            .body(errorResponse);
  }

  // Valid 검증 실패 예외 처리
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseBody
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e){
    log.warn("[GlobalExceptionHandler] MethodArgumentNotValidException 발생: {}", e.getMessage(), e);

    Map<String, Object> details = new HashMap<>();

    // 각 필드별 오류를 개별적으로 details에 추가
    // 키는 필드명, 값은 오류 메시지만
    e.getBindingResult().getFieldErrors().forEach(error -> {
      String fieldName = error.getField(); // 필드명 (예: "password")
      String errorMessage = error.getDefaultMessage() != null ?
          error.getDefaultMessage() : "유효성 검증 실패"; // 오류 메시지만
      details.put(fieldName, errorMessage);
    });

    // 글로벌 오류 메시지 수집
    List<String> globalErrors = e.getBindingResult().getGlobalErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .toList();

    if (!globalErrors.isEmpty()) {
      details.put("globalErrors", String.join(", ", globalErrors));
    }

    ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(Instant.now())
            .code("INVALID_INPUT_VALUE")
            .message("잘못된 입력값입니다.")
            .details(details)
            .exceptionType("MethodArgumentNotValidException")
            .status(400)
            .build();

    return ResponseEntity
            .badRequest()
            .body(errorResponse);
  }

  // 필수 요청 헤더가 누락된 경우 처리
  @ExceptionHandler(org.springframework.web.bind.MissingRequestHeaderException.class)
  @ResponseBody
  public ResponseEntity<ErrorResponse> handleMissingRequestHeader(MissingRequestHeaderException e) {
    log.warn("[GlobalExceptionHandler] MissingRequestHeaderException 발생: {}", e.getMessage(), e);

    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(Instant.now())
        .code("MISSING_HEADER")
        .message("필수 헤더가 누락되었습니다: " + e.getHeaderName())
        .exceptionType("MissingRequestHeaderException")
        .status(HttpStatus.BAD_REQUEST.value())
        .build();

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(errorResponse);
  }

  // 잘못된 타입의 요청 파라미터가 전달된 경우 처리
  @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
  @ResponseBody
  public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
    log.warn("[GlobalExceptionHandler] MethodArgumentTypeMismatchException 발생: {}", e.getMessage(), e);

    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(Instant.now())
        .code("INVALID_PARAM_TYPE")
        .message("잘못된 파라미터 타입입니다: " + e.getName())
        .exceptionType("MethodArgumentTypeMismatchException")
        .status(HttpStatus.BAD_REQUEST.value())
        .build();

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(errorResponse);
  }

  // ConstraintViolationException 예외 처리
  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseBody
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
    log.warn("[GlobalExceptionHandler] ConstraintViolationException 발생: {}", e.getMessage(), e);

    Map<String, Object> details = new HashMap<>();

    for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
      String propertyPath = violation.getPropertyPath().toString(); // 예: "getNotifications.limit"
      String message = violation.getMessage();                      // 예: "limit은 1 이상의 정수"
      details.put(propertyPath, message);
    }

    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(Instant.now())
        .code("INVALID_CONSTRAINT")
        .message("잘못된 요청입니다.")
        .details(details)
        .exceptionType("ConstraintViolationException")
        .status(400)
        .build();

    return ResponseEntity
        .badRequest()
        .body(errorResponse);
  }

  // defalt 예외 처리기
  @ExceptionHandler(Exception.class)
  @ResponseBody
  public ResponseEntity<String> handleException(Exception ex) {
    log.warn("[GlobalExceptionHandler] 서버 내부 오류 발생", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("서버 내부 오류: " + ex.getMessage());
  }
}