package com.newsummarize.backend.error;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponse {
    private final int status;
    private final HttpStatus code;
    private final String message;

    @Builder
    private ErrorResponse(int status, HttpStatus code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public static ErrorResponse of(int status, HttpStatus code, String message) {
        return ErrorResponse.builder()
                .status(status)
                .code(code)
                .message(message)
                .build();
    }
}