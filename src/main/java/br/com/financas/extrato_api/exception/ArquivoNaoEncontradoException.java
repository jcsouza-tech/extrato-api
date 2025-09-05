package br.com.financas.extrato_api.exception;

public class ArquivoNaoEncontradoException extends RuntimeException {
    
    public ArquivoNaoEncontradoException(String message) {
        super(message);
    }
    
    public ArquivoNaoEncontradoException(String message, Throwable cause) {
        super(message, cause);
    }
}
