package com.fiap.mecanica.shared.exception;

import com.fiap.mecanica.shared.exception.dto.ExceptionDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final int BAD_REQUEST = 400;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDto> exceptionHandler(Exception e) {
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR).body(
                        new ExceptionDto(INTERNAL_SERVER_ERROR, LocalDateTime.now().format(FORMATTER), "Ocorreu um erro inesperado: " + e.getMessage())
                );
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ExceptionDto> baseExceptionHandler(BaseException e) {
        return ResponseEntity
                .status(e.getStatusCode()).body(
                        new ExceptionDto(e.getStatusCode(), LocalDateTime.now().format(FORMATTER), e.getMessage())
                );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionDto> illegalArgumentExceptionHandler(IllegalArgumentException e){

        return ResponseEntity.status(BAD_REQUEST).body(
                new ExceptionDto(BAD_REQUEST, LocalDateTime.now().format(FORMATTER), e.getMessage())
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionDto> dataIntegrityViolationExceptionHandler(DataIntegrityViolationException e){

        return ResponseEntity.status(BAD_REQUEST).body(
                new ExceptionDto(BAD_REQUEST, LocalDateTime.now().format(FORMATTER), e.getMessage())
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionDto> httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException e){

        return ResponseEntity.status(BAD_REQUEST).body(
                new ExceptionDto(BAD_REQUEST, LocalDateTime.now().format(FORMATTER), "Método " + e.getMethod() + " não suportado")
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getGlobalErrors().forEach(error ->
                errors.put(error.getObjectName(), error.getDefaultMessage())
        );

        if (errors.isEmpty()) {
            ex.getBindingResult().getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
        }

        return ResponseEntity.status(BAD_REQUEST).body(errors);
    }

}
