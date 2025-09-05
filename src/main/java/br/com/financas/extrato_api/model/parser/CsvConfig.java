package br.com.financas.extrato_api.model.parser;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CsvConfig {
    private String separator;
    private String dateFormat;
    private String dateRegex;
    private String valueRegex;
    private Integer skipLine;
    private String pais;
    private String idioma;
}