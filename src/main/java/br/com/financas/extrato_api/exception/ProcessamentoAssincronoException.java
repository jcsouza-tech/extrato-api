package br.com.financas.extrato_api.exception;

/**
 * Exceção para erros relacionados ao processamento assíncrono
 */
public class ProcessamentoAssincronoException extends RuntimeException {

    public ProcessamentoAssincronoException(String message) {
        super(message);
    }

    public ProcessamentoAssincronoException(String message, Throwable cause) {
        super(message, cause);
    }
}
