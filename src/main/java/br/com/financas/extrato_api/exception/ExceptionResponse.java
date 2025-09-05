package br.com.financas.extrato_api.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
public class ExceptionResponse {
    @Getter private final LocalDateTime timestamp;
    @Getter private final String message;
    @Getter private final String details;
}
