package br.com.financas.extrato_api;

import br.com.financas.extrato_api.config.parser.BancoDoBrasilParserConfig;
import br.com.financas.extrato_api.config.BancosSuportadosConfig;
import br.com.financas.extrato_api.config.parser.ItauParserConfig;
import br.com.financas.extrato_api.config.parser.ParserAutoDiscoveryConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({BancoDoBrasilParserConfig.class,
        ItauParserConfig.class, ParserAutoDiscoveryConfig.class,
        BancosSuportadosConfig.class
})
public class ExtratoApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExtratoApiApplication.class, args);
    }
}