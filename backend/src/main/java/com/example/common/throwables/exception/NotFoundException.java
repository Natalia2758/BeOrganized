package com.example.common.throwables.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String data, Long id) {
        super(data + " with id " + id + " was not found");
    }
    
}
