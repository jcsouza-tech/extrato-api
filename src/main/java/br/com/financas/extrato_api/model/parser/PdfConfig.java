package br.com.financas.extrato_api.model.parser;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PdfConfig {
    private String dateFormat;
    private String dateRegex;
    private String valueRegex;
    private String transactionRegex;
    private String headerRegex;
    private String skipPattern;
    private String pais;
    private String idioma;
}