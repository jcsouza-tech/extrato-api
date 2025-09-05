package br.com.financas.extrato_api.config.parser;

import br.com.financas.extrato_api.model.parser.CsvConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "parser.config.banco-do-brasil")
public class BancoDoBrasilParserConfig extends BankParserConfig {
    private CsvConfig csv;
}