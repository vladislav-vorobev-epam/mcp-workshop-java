package com.epam.masterclass.controller;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.epam.masterclass.exception.InvalidStatusTransitionException;
import com.epam.masterclass.exception.TaskNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles TaskNotFoundException.
     * Returns 404 Not Found.
     */
    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFoundException(TaskNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles InvalidStatusTransitionException.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatusTransitionException(
            InvalidStatusTransitionException ex
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                null
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles validation errors from @Valid annotation.
     * Returns 400 Bad Request with field-level error details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        List<ValidationError> validationErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = error instanceof FieldError
                            ? ((FieldError) error).getField()
                            : error.getObjectName();
                    String message = error.getDefaultMessage();
                    return new ValidationError(fieldName, message);
                })
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Validation failed",
                validationErrors
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles all other unexpected exceptions.
     * Returns 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Standard error response structure.
     */
    public record ErrorResponse(
            String timestamp,
            int status,
            String error,
            String message,
            List<ValidationError> errors
    ) {
    }

    /**
     * Validation error details.
     */
    public record ValidationError(
            String field,
            String message
    ) {
    }
}
