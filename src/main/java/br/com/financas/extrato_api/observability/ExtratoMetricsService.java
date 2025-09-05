package br.com.financas.extrato_api.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Service para métricas customizadas do extrato
 */
@Service
public class ExtratoMetricsService {
    
    private final Counter arquivosProcessados;
    private final Counter transacoesProcessadas;
    private final Counter arquivosDuplicados;
    private final Counter errosProcessamento;
    private final Timer tempoProcessamento;
    private final Counter bancosUtilizados;
    
    public ExtratoMetricsService(MeterRegistry meterRegistry) {
        this.arquivosProcessados = Counter.builder("extrato.arquivos.processados")
                .description("Total de arquivos processados")
                .register(meterRegistry);
                
        this.transacoesProcessadas = Counter.builder("extrato.transacoes.processadas")
                .description("Total de transações processadas")
                .register(meterRegistry);
                
        this.arquivosDuplicados = Counter.builder("extrato.arquivos.duplicados")
                .description("Total de arquivos duplicados detectados")
                .register(meterRegistry);
                
        this.errosProcessamento = Counter.builder("extrato.erros.processamento")
                .description("Total de erros no processamento")
                .register(meterRegistry);
                
        this.tempoProcessamento = Timer.builder("extrato.tempo.processamento")
                .description("Tempo de processamento de arquivos")
                .register(meterRegistry);
                
        this.bancosUtilizados = Counter.builder("extrato.bancos.utilizados")
                .description("Bancos utilizados no processamento")
                .tag("banco", "unknown")
                .register(meterRegistry);
    }
    
    public void incrementarArquivosProcessados() {
        arquivosProcessados.increment();
    }
    
    public void incrementarTransacoesProcessadas(int quantidade) {
        transacoesProcessadas.increment(quantidade);
    }
    
    public void incrementarArquivosDuplicados() {
        arquivosDuplicados.increment();
    }
    
    public void incrementarErrosProcessamento() {
        errosProcessamento.increment();
    }
    
    public void registrarTempoProcessamento(Duration duracao) {
        tempoProcessamento.record(duracao);
    }
    
    public void incrementarBancoUtilizado(String banco) {
        bancosUtilizados.increment();
    }
}
