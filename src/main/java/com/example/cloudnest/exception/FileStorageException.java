package com.example.cloudnest.exception;

public class FileStorageException extends RuntimeException {
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
