package br.com.financas.extrato_api.config.parser;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "parser.auto-discovery")
public class ParserAutoDiscoveryConfig {
    private String basePackage;
    private boolean enabled;
    private int scanDepth;
}
