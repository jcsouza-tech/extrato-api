package br.com.financas.extrato_api.service;

import br.com.financas.extrato_api.config.BancosSuportadosConfig;
import br.com.financas.extrato_api.exception.BancoNaoSuportadoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Service Locator que usa o contexto Spring para instanciar services automaticamente
 */
@Component
public class ExtratoServiceLocator {
    
    private final ApplicationContext applicationContext;
    private final BancosSuportadosConfig bancosConfig;
    
    @Autowired
    public ExtratoServiceLocator(ApplicationContext applicationContext, 
                               BancosSuportadosConfig bancosConfig) {
        this.applicationContext = applicationContext;
        this.bancosConfig = bancosConfig;
    }
    
    /**
     * Retorna o service para o banco especificado
     * @param nomeBanco nome do banco
     * @return ExtratoService correspondente
     * @throws BancoNaoSuportadoException se o banco não for suportado
     */
    public ExtratoService getService(String nomeBanco) {
        if (nomeBanco == null || nomeBanco.trim().isEmpty()) {
            throw new BancoNaoSuportadoException("Nome do banco não pode ser vazio");
        }
        
        String bancoNormalizado = nomeBanco.toLowerCase().trim();
        
        // Verifica se o banco está na configuração
        if (!bancosConfig.getNames().contains(bancoNormalizado)) {
            throw new BancoNaoSuportadoException(nomeBanco);
        }
        
        // Constrói o nome do bean seguindo o padrão {nome-banco}-service
        String beanName = bancoNormalizado + "-service";
        
        try {
            // Busca o service no contexto Spring
            return applicationContext.getBean(beanName, ExtratoService.class);
        } catch (Exception e) {
            throw new BancoNaoSuportadoException("Service não encontrado para banco: " + nomeBanco + " (bean: " + beanName + ")");
        }
    }
    
    /**
     * Retorna a lista de bancos suportados da configuração
     * @return lista de nomes de bancos suportados
     */
    public List<String> getBancosSuportados() {
        return bancosConfig.getNames();
    }
}
