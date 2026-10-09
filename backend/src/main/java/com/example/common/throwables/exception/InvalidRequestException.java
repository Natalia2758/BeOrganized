package com.example.common.throwables.exception;

/**
 * <b>InvalidRequestException</b><br>
 * 
 * An exception class that inherits from the RuntimeException class.
 * It is thrown during form validation of user input.
 */
public class InvalidRequestException extends RuntimeException{
    private String code;
    public InvalidRequestException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() { return code; }
}
