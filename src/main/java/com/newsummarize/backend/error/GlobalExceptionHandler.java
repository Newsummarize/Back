package com.newsummarize.backend.error;

import com.newsummarize.backend.error.exception.InternalFlaskErrorException;
import com.newsummarize.backend.error.exception.InvalidRequestParameterException;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestKeywordException(InvalidRequestParameterException e) {
        System.out.println("[Log] >>> 검색어 또는 기간 값이 잘못된 요청.");
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(400, HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(InternalFlaskErrorException.class)
    public ResponseEntity<ErrorResponse> handleInternalFlaskErrorException(InternalFlaskErrorException e) {
        System.out.println("[Log] >>> Flask 서버가 비정상적인 응답을 반환함.");
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(500, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUndefinedException(Exception e) {
        System.out.println("[Log] >>> 알 수 없는 오류.");
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(500, HttpStatus.INTERNAL_SERVER_ERROR, "예기치 못한 문제가 발생했습니다."));
    }
}
