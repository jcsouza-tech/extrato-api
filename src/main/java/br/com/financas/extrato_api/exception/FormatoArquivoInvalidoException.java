package br.com.financas.extrato_api.exception;

public class FormatoArquivoInvalidoException extends RuntimeException {
    
    public FormatoArquivoInvalidoException(String message) {
        super(message);
    }
    
    public FormatoArquivoInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
