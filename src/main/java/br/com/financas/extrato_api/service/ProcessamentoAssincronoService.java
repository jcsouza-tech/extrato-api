package br.com.financas.extrato_api.service;

import br.com.financas.extrato_api.config.RabbitMQConfig;
import br.com.financas.extrato_api.exception.ProcessamentoAssincronoException;
import br.com.financas.extrato_api.model.dto.ProcessamentoMessage;
import br.com.financas.extrato_api.model.dto.ProcessamentoStatusDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Serviço para processamento assíncrono de arquivos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessamentoAssincronoService {

    private final RabbitTemplate rabbitTemplate;
    private final ProcessamentoStatusService statusService;

    /**
     * Envia arquivo para processamento assíncrono
     */
    public ProcessamentoStatusDTO enviarParaProcessamento(String banco, String nomeArquivo, 
                                                         byte[] conteudoArquivo) {
        
        try {
            // Validações básicas
            if (banco == null || banco.trim().isEmpty()) {
                throw new ProcessamentoAssincronoException("Banco não pode ser nulo ou vazio");
            }
            
            if (nomeArquivo == null || nomeArquivo.trim().isEmpty()) {
                throw new ProcessamentoAssincronoException("Nome do arquivo não pode ser nulo ou vazio");
            }
            
            if (conteudoArquivo == null || conteudoArquivo.length == 0) {
                throw new ProcessamentoAssincronoException("Conteúdo do arquivo não pode ser nulo ou vazio");
            }
            
            // Calcular hash do arquivo
            String hashArquivo = java.util.Arrays.toString(java.security.MessageDigest.getInstance("MD5")
                    .digest(conteudoArquivo));
            
            UUID processamentoId = UUID.randomUUID();
            
            ProcessamentoMessage message = ProcessamentoMessage.builder()
                    .processamentoId(processamentoId)
                    .banco(banco)
                    .nomeArquivo(nomeArquivo)
                    .conteudoArquivo(conteudoArquivo)
                    .hashArquivo(hashArquivo)
                    .dataEnvio(LocalDateTime.now())
                    .status(ProcessamentoMessage.ProcessamentoStatus.PENDENTE)
                    .prioridade("NORMAL")
                    .build();

            // Enviar para fila de processamento
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_PROCESSAMENTO,
                    RabbitMQConfig.ROUTING_KEY_PROCESSAMENTO,
                    message
            );

            log.info("Arquivo enviado para processamento assíncrono: {} (ID: {})", nomeArquivo, processamentoId);

            // Criar status inicial
            ProcessamentoStatusDTO status = ProcessamentoStatusDTO.builder()
                    .processamentoId(processamentoId)
                    .banco(banco)
                    .nomeArquivo(nomeArquivo)
                    .status(ProcessamentoMessage.ProcessamentoStatus.PENDENTE)
                    .dataInicio(LocalDateTime.now())
                    .progresso(0)
                    .mensagem("Arquivo enviado para processamento")
                    .build();

            statusService.salvarStatus(status);

            return status;
            
        } catch (ProcessamentoAssincronoException e) {
            throw e; // Re-lançar exceções específicas
        } catch (Exception e) {
            log.error("Erro inesperado ao enviar arquivo para processamento: {}", e.getMessage(), e);
            throw new ProcessamentoAssincronoException("Erro interno ao processar arquivo: " + e.getMessage(), e);
        }
    }

    /**
     * Consulta status do processamento
     */
    @Cacheable(value = "processamentoStatus", key = "#processamentoId")
    public ProcessamentoStatusDTO consultarStatus(UUID processamentoId) {
        if (processamentoId == null) {
            throw new ProcessamentoAssincronoException("ID do processamento não pode ser nulo");
        }
        
        ProcessamentoStatusDTO status = statusService.consultarStatus(processamentoId);
        if (status == null) {
            throw new ProcessamentoAssincronoException("Processamento não encontrado: " + processamentoId);
        }
        
        return status;
    }

    /**
     * Cancela processamento pendente
     */
    public boolean cancelarProcessamento(UUID processamentoId) {
        if (processamentoId == null) {
            throw new ProcessamentoAssincronoException("ID do processamento não pode ser nulo");
        }
        
        try {
            ProcessamentoStatusDTO status = statusService.consultarStatus(processamentoId);
            
            if (status == null) {
                throw new ProcessamentoAssincronoException("Processamento não encontrado: " + processamentoId);
            }
            
            if (status.getStatus() != ProcessamentoMessage.ProcessamentoStatus.PENDENTE) {
                throw new ProcessamentoAssincronoException("Processamento não pode ser cancelado. Status atual: " + status.getStatus());
            }

            status.setStatus(ProcessamentoMessage.ProcessamentoStatus.CANCELADO);
            status.setDataFim(LocalDateTime.now());
            status.setMensagem("Processamento cancelado pelo usuário");
            
            statusService.atualizarStatus(status);
            
            log.info("Processamento cancelado: {}", processamentoId);
            return true;
            
        } catch (ProcessamentoAssincronoException e) {
            throw e; // Re-lançar exceções específicas
        } catch (Exception e) {
            log.error("Erro inesperado ao cancelar processamento: {}", e.getMessage(), e);
            throw new ProcessamentoAssincronoException("Erro interno ao cancelar processamento: " + e.getMessage(), e);
        }
    }

    /**
     * Lista todos os processamentos
     */
    public List<ProcessamentoStatusDTO> listarProcessamentos() {
        try {
            return statusService.listarTodosProcessamentos();
        } catch (Exception e) {
            log.error("Erro inesperado ao listar processamentos: {}", e.getMessage(), e);
            throw new ProcessamentoAssincronoException("Erro interno ao listar processamentos: " + e.getMessage(), e);
        }
    }
}
