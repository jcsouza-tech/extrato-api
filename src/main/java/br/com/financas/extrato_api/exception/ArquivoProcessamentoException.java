package br.com.financas.extrato_api.exception;

public class ArquivoProcessamentoException extends RuntimeException {
    
    public ArquivoProcessamentoException(String message) {
        super(message);
    }
    
    public ArquivoProcessamentoException(String message, Throwable cause) {
        super(message, cause);
    }
}
