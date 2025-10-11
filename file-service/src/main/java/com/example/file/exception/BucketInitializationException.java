package com.example.file.exception;

public class BucketInitializationException extends RuntimeException {
    public BucketInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
