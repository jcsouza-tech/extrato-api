package br.com.financas.extrato_api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "bancos.suporte")
public class BancosSuportadosConfig {
    private List<String> names;
}
