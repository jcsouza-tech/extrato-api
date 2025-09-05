package br.com.financas.extrato_api.config.parser;

import br.com.financas.extrato_api.model.parser.PdfConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "parser.config.itau")
public class ItauParserConfig extends BankParserConfig {
    private PdfConfig pdf;
}