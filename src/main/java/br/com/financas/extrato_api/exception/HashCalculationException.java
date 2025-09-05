package br.com.financas.extrato_api.exception;

public class HashCalculationException extends RuntimeException {
    public HashCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}
