package com.fiap.mecanica.shared.exception;

import com.fiap.mecanica.shared.exception.dto.ExceptionDto;
import com.fiap.mecanica.shared.seguranca.core.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerUnitTest {

    private static final String TIMESTAMP_PATTERN = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @Test
    void exceptionHandler_shouldReturnInternalServerError() {
        Exception exception = new Exception("erro genérico");

        ResponseEntity<ExceptionDto> response = handler.exceptionHandler(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getBody().status());
        assertEquals("Ocorreu um erro inesperado: erro genérico", response.getBody().message());
        assertTrue(response.getBody().timestamp().matches(TIMESTAMP_PATTERN));
    }

    @Test
    void baseExceptionHandler_shouldReturnStatusAndMessageFromException() {
        UserNotFoundException exception = new UserNotFoundException();

        ResponseEntity<ExceptionDto> response = handler.baseExceptionHandler(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getBody().status());
        assertEquals("Usuario nao encontrado", response.getBody().message());
        assertTrue(response.getBody().timestamp().matches(TIMESTAMP_PATTERN));
    }

    @Test
    void illegalArgumentExceptionHandler_shouldReturnBadRequest() {
        IllegalArgumentException exception = new IllegalArgumentException("argumento inválido");

        ResponseEntity<ExceptionDto> response = handler.illegalArgumentExceptionHandler(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getBody().status());
        assertEquals("argumento inválido", response.getBody().message());
        assertTrue(response.getBody().timestamp().matches(TIMESTAMP_PATTERN));
    }

    @Test
    void dataIntegrityViolationExceptionHandler_shouldReturnBadRequest() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("violação de integridade");

        ResponseEntity<ExceptionDto> response = handler.dataIntegrityViolationExceptionHandler(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getBody().status());
        assertEquals("violação de integridade", response.getBody().message());
        assertTrue(response.getBody().timestamp().matches(TIMESTAMP_PATTERN));
    }

    @Test
    void httpRequestMethodNotSupportedExceptionHandler_shouldReturnBadRequestWithMethodName() {
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("DELETE");

        ResponseEntity<ExceptionDto> response = handler.httpRequestMethodNotSupportedExceptionHandler(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getBody().status());
        assertEquals("Método DELETE não suportado", response.getBody().message());
        assertTrue(response.getBody().timestamp().matches(TIMESTAMP_PATTERN));
    }

    @Test
    void methodArgumentNotValidExceptionHandler_shouldReturnMapOfFieldErrors() {
        FieldError fieldError = new FieldError("object", "email", "não pode ser nulo");

        Mockito.when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, String>> response = handler.methodArgumentNotValidExceptionHandler(methodArgumentNotValidException);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("não pode ser nulo", response.getBody().get("email"));
    }
}
