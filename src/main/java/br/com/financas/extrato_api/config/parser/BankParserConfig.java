package br.com.financas.extrato_api.config.parser;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
@Getter
@Setter
public abstract class BankParserConfig {
    private String name;
    private List<String> filePatterns;
    private List<String> supportedExtensions;
}