package br.com.financas.extrato_api.exception;

public class BancoNaoSuportadoException extends RuntimeException {
    
    public BancoNaoSuportadoException(String banco) {
        super("Banco não suportado: " + banco);
    }
    
    public BancoNaoSuportadoException(String banco, Throwable cause) {
        super("Banco não suportado: " + banco, cause);
    }
}
