package br.com.financas.extrato_api.service;

import br.com.financas.extrato_api.model.dto.ProcessamentoMessage;
import br.com.financas.extrato_api.model.dto.ProcessamentoStatusDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço para gerenciar status de processamento
 */
@Slf4j
@Service
public class ProcessamentoStatusService {

    // Cache em memória para status de processamento
    private final Map<UUID, ProcessamentoStatusDTO> statusCache = new ConcurrentHashMap<>();

    /**
     * Salva status inicial do processamento
     */
    @CachePut(value = "processamentoStatus", key = "#status.processamentoId")
    public ProcessamentoStatusDTO salvarStatus(ProcessamentoStatusDTO status) {
        statusCache.put(status.getProcessamentoId(), status);
        log.debug("Status salvo: {} - {}", status.getProcessamentoId(), status.getStatus());
        return status;
    }

    /**
     * Atualiza status do processamento
     */
    @CachePut(value = "processamentoStatus", key = "#status.processamentoId")
    public ProcessamentoStatusDTO atualizarStatus(ProcessamentoStatusDTO status) {
        statusCache.put(status.getProcessamentoId(), status);
        log.debug("Status atualizado: {} - {}", status.getProcessamentoId(), status.getStatus());
        return status;
    }

    /**
     * Consulta status do processamento
     */
    @Cacheable(value = "processamentoStatus", key = "#processamentoId")
    public ProcessamentoStatusDTO consultarStatus(UUID processamentoId) {
        return statusCache.get(processamentoId);
    }

    /**
     * Atualiza progresso do processamento
     */
    public void atualizarProgresso(UUID processamentoId, Integer progresso, String mensagem) {
        ProcessamentoStatusDTO status = statusCache.get(processamentoId);
        if (status != null) {
            status.setProgresso(progresso);
            status.setMensagem(mensagem);
            statusCache.put(processamentoId, status);
            log.debug("Progresso atualizado: {} - {}% - {}", processamentoId, progresso, mensagem);
        }
    }

    /**
     * Marca processamento como concluído
     */
    public void marcarConcluido(UUID processamentoId, Integer transacoesProcessadas, 
                               Integer transacoesSalvas, Integer duplicatasIgnoradas, Long uploadId) {
        ProcessamentoStatusDTO status = statusCache.get(processamentoId);
        if (status != null) {
            status.setStatus(ProcessamentoMessage.ProcessamentoStatus.CONCLUIDO);
            status.setDataFim(LocalDateTime.now());
            status.setProgresso(100);
            status.setTransacoesProcessadas(transacoesProcessadas);
            status.setTransacoesSalvas(transacoesSalvas);
            status.setDuplicatasIgnoradas(duplicatasIgnoradas);
            status.setUploadId(uploadId);
            status.setMensagem("Processamento concluído com sucesso");
            
            // Calcular tempo de processamento
            if (status.getDataInicio() != null) {
                long tempoMs = java.time.Duration.between(status.getDataInicio(), status.getDataFim()).toMillis();
                status.setTempoProcessamentoMs(tempoMs);
                
                // Calcular velocidade de processamento
                if (tempoMs > 0 && transacoesProcessadas > 0) {
                    double velocidade = (transacoesProcessadas * 1000.0) / tempoMs;
                    status.setVelocidadeProcessamento(velocidade);
                }
            }
            
            statusCache.put(processamentoId, status);
            log.info("Processamento concluído: {} - {} transações processadas", processamentoId, transacoesProcessadas);
        }
    }

    /**
     * Marca processamento com erro
     */
    public void marcarErro(UUID processamentoId, String erro) {
        ProcessamentoStatusDTO status = statusCache.get(processamentoId);
        if (status != null) {
            status.setStatus(ProcessamentoMessage.ProcessamentoStatus.ERRO);
            status.setDataFim(LocalDateTime.now());
            status.setErro(erro);
            status.setMensagem("Erro no processamento: " + erro);
            statusCache.put(processamentoId, status);
            log.error("Processamento com erro: {} - {}", processamentoId, erro);
        }
    }

    /**
     * Remove status do cache (limpeza)
     */
    @CacheEvict(value = "processamentoStatus", key = "#processamentoId")
    public void removerStatus(UUID processamentoId) {
        statusCache.remove(processamentoId);
        log.debug("Status removido: {}", processamentoId);
    }

    /**
     * Lista todos os processamentos
     */
    public List<ProcessamentoStatusDTO> listarTodosProcessamentos() {
        return statusCache.values().stream()
                .sorted((a, b) -> {
                    // Ordenar por data de início (mais recente primeiro)
                    if (a.getDataInicio() == null && b.getDataInicio() == null) return 0;
                    if (a.getDataInicio() == null) return 1;
                    if (b.getDataInicio() == null) return -1;
                    return b.getDataInicio().compareTo(a.getDataInicio());
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
