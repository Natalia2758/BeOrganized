package com.example.common.throwables.exception;

/**
 * 
 * <b>DuplicateEmailException</b><br>
 * 
 * An exception class that inherits from the RuntimeException class. It is thrown when the user with the given email already exists.
 * 
 */
public class DuplicateEmailException extends RuntimeException{
    public DuplicateEmailException(String email) {
        super("The email " + email + " already exists");
    }
}
