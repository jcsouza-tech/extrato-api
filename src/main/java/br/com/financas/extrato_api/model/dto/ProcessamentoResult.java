package br.com.financas.extrato_api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProcessamentoResult {
    private String nomeArquivo;
    private boolean sucesso;
    private String mensagem;
    private int transacoesSalvas;

    public static ProcessamentoResult sucesso(String nomeArquivo, int transacoesSalvas) {
        return new ProcessamentoResult(nomeArquivo, true, "Arquivo processado com sucesso", transacoesSalvas);
    }

    public static ProcessamentoResult arquivoDuplicado(String nomeArquivo) {
        return new ProcessamentoResult(nomeArquivo, false, "Arquivo já foi processado anteriormente", 0);
    }

    public static ProcessamentoResult parserNaoEncontrado(String nomeArquivo) {
        return new ProcessamentoResult(nomeArquivo, false, "Parser não encontrado para este arquivo", 0);
    }

    public static ProcessamentoResult erroProcessamento(String nomeArquivo, String erro) {
        return new ProcessamentoResult(nomeArquivo, false, "Erro no processamento: " + erro, 0);
    }
}