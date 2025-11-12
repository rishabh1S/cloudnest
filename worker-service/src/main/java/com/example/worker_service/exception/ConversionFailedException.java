package com.example.worker_service.exception;

public class ConversionFailedException extends RuntimeException {
    public ConversionFailedException(String message){
        super(message);
    }
}
