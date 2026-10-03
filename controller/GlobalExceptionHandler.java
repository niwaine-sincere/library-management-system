package com.librarymanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> api(
            ApiException exception
    ) {

        return ResponseEntity
                .status(exception.getStatus())
                .body(
                        Map.of(
                                "error",
                                exception.getMessage()
                        )
                );
    }


    @ExceptionHandler(
            IllegalArgumentException.class
    )
    public ResponseEntity<Map<String, String>> illegal(
            IllegalArgumentException exception
    ) {

        return ResponseEntity
                .badRequest()
                .body(
                        Map.of(
                                "error",
                                exception.getMessage()
                        )
                );
    }


    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    public ResponseEntity<Map<String, String>>
    validation(
            MethodArgumentNotValidException exception
    ) {

        String message =
                exception
                        .getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .findFirst()
                        .map(
                                error ->
                                        error.getField()
                                                + ": "
                                                + error.getDefaultMessage()
                        )
                        .orElse(
                                "Invalid input."
                        );


        return ResponseEntity
                .badRequest()
                .body(
                        Map.of(
                                "error",
                                message
                        )
                );
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> other(
            Exception exception
    ) {

        return ResponseEntity
                .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
                .body(
                        Map.of(
                                "error",
                                "Server error: "
                                        + exception.getMessage()
                        )
                );
    }
}