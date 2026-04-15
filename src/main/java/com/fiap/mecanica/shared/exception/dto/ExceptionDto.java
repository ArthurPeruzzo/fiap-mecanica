package com.fiap.mecanica.shared.exception.dto;

import org.springframework.http.HttpStatus;

public record ExceptionDto(HttpStatus status, String timestamp, String message) {
}
