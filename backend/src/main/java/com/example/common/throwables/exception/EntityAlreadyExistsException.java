package com.example.common.throwables.exception;

/**
 * 
 * <b>EntityAlreadyExistsException</b><br>
 * 
 * An exception class that inherits from the RuntimeException class.
 * It is thrown when an entity with the given attribute value already exists in the database.
 */
public class EntityAlreadyExistsException extends RuntimeException {
    public EntityAlreadyExistsException(String entity, String attribute) {
        super(entity + " with this " + attribute + " already exists.");
    }
}
