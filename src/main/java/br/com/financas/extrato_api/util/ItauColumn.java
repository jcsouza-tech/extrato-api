package br.com.financas.extrato_api.util;

public enum ItauColumn {
    DATA(0, "Data (DD/MM/YYYY)"),
    DESCRICAO(1, "Descrição da Transação"),
    VALOR(2, "Valor Monetário"),
    SALDO(3, "Saldo da Conta");

    private final int index;
    private final String description;

    ItauColumn(int index, String description) {
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