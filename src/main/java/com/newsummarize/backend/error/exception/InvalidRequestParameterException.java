package com.newsummarize.backend.error.exception;

public class InvalidRequestParameterException extends RuntimeException {
    public InvalidRequestParameterException(String message) {
        super(message);
    }
}