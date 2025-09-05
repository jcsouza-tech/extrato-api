package br.com.financas.extrato_api.util;

public enum CsvColumn {
    DATA(0, "Data (DD/MM/YYYY)"),
    LANCAMENTO(1, "Descrição do Lançamento"),
    DETALHES(2, "Detalhes Adicionais"),
    NUMERO_DOCUMENTO(3, "Número do Documento"),
    VALOR(4, "Valor Monetário"),
    TIPO_LANCAMENTO(5, "Tipo de Lançamento");

    private final int index;
    private final String description;

    CsvColumn(int index, String description) {
        this.index = index;
        this.description = description;
    }

    public int getIndex() {
        return index;
    }

    public String getDescription() {
        return description;
    }
}
