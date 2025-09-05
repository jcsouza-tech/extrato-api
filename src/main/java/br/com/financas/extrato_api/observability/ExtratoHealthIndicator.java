package br.com.financas.extrato_api.observability;

import br.com.financas.extrato_api.repository.TransacaoRepository;
import br.com.financas.extrato_api.repository.UploadArquivoRepository;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health check customizado para o extrato
 */
@Component
public class ExtratoHealthIndicator implements HealthIndicator {
    
    private final TransacaoRepository transacaoRepository;
    private final UploadArquivoRepository uploadArquivoRepository;
    
    public ExtratoHealthIndicator(TransacaoRepository transacaoRepository,
                                UploadArquivoRepository uploadArquivoRepository) {
        this.transacaoRepository = transacaoRepository;
        this.uploadArquivoRepository = uploadArquivoRepository;
    }
    
    @Override
    public Health health() {
        try {
            // Verifica conectividade com o banco
            long totalTransacoes = transacaoRepository.count();
            long totalUploads = uploadArquivoRepository.count();
            
            return Health.up()
                    .withDetail("transacoes", totalTransacoes)
                    .withDetail("uploads", totalUploads)
                    .withDetail("status", "Sistema funcionando normalmente")
                    .build();
                    
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("status", "Sistema com problemas")
                    .build();
        }
    }
}
