package br.com.financas.extrato_api.model.dto;

import br.com.financas.extrato_api.model.Transacao;

import java.time.format.DateTimeFormatter;

public class TransacaoDTO {
    private String data;
    private String lancamento;
    private String detalhes;
    private String numeroDocumento;
    private String valor;
    private String tipoLancamento;

    private String categoria;

    public static TransacaoDTO from(Transacao t) {
        TransacaoDTO dto = new TransacaoDTO();
        dto.data = t.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        dto.lancamento = t.getLancamento();
        dto.detalhes = t.getDetalhes();
        dto.numeroDocumento = t.getNumeroDocumento();
        dto.valor = String.format("R$ %.2f", t.getValor());
        dto.tipoLancamento = t.getTipoLancamento();
        dto.categoria = t.getCategoria();
        return dto;
    }

    public String getData() { return data; }
    public String getLancamento() { return lancamento; }
    public String getDetalhes() { return detalhes; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getValor() { return valor; }
    public String getTipoLancamento() { return tipoLancamento; }
    public String getCategoria() {
        return categoria;
    }
}
