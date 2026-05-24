package com.springlms.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity
                .status(resolveStatus(exception.getMessage()))
                .body(Map.of("message", exception.getMessage()));
    }

    private HttpStatus resolveStatus(String message) {
        if (message == null) {
            return HttpStatus.BAD_REQUEST;
        }

        String normalizedMessage = message.toLowerCase();

        if (normalizedMessage.contains("pending admin approval")
                || normalizedMessage.contains("not active")) {
            return HttpStatus.FORBIDDEN;
        }

        if (normalizedMessage.contains("invalid email or password")) {
            return HttpStatus.UNAUTHORIZED;
        }

        if (normalizedMessage.contains("not found")) {
            return HttpStatus.NOT_FOUND;
        }

        return HttpStatus.BAD_REQUEST;
    }
}
