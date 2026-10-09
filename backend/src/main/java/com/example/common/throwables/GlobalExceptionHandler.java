package com.example.common.throwables;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.common.throwables.error.ApiError;
import com.example.common.throwables.exception.EntityAlreadyExistsException;
import com.example.common.throwables.exception.InvalidRequestException;
import com.example.common.throwables.exception.NotFoundException;
import com.example.common.throwables.exception.TimeBlockOccupiedException;
import com.example.common.throwables.exception.DuplicateEmailException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(NotFoundException exception) {

        ApiError error = new ApiError("NOT_FOUND", exception.getMessage());

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(error);
    }

    @ExceptionHandler(TimeBlockOccupiedException.class)
    public ResponseEntity<ApiError> handleTimeBlockOccupied(TimeBlockOccupiedException exception) {
        ApiError error = new ApiError("TIME_BLOCK_OCCUPIED", exception.getMessage());

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception
            .getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("Validation failed");

        ApiError error = new ApiError("VALIDATION_ERROR", message);

        return ResponseEntity
            .badRequest()
            .body(error);
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleExistingEntity(EntityAlreadyExistsException exception) {
        ApiError error = new ApiError("ENTITY_EXISTS", exception.getMessage());
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(error);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiError> handleDuplicateEmails(DuplicateEmailException exception) {
        ApiError error = new ApiError("DUPLICATE_EMAILS", exception.getMessage());
        return ResponseEntity
            .badRequest()
            .body(error);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiError> handleInvalidRequest(InvalidRequestException exception) {
        ApiError error = new ApiError(exception.getCode(), exception.getMessage());
        return ResponseEntity
            .badRequest()
            .body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException exception) {
        ApiError error = new ApiError("ILLEGAL_STATE", exception.getMessage());
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        ApiError error = new ApiError("DATA_INTEGRITY_VIOLATION", "The request could not be saved due to a database constraint.");
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(error);
    }
}
