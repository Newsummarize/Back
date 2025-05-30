package com.newsummarize.backend.error.exception;

public class InternalFlaskErrorException extends RuntimeException {
    public InternalFlaskErrorException(String message) {
        super(message);
    }
}