package br.com.financas.extrato_api.service;

import br.com.financas.extrato_api.config.RabbitMQConfig;
import br.com.financas.extrato_api.model.dto.ProcessamentoMessage;
import br.com.financas.extrato_api.model.dto.ProcessamentoResult;
import br.com.financas.extrato_api.model.dto.ProcessamentoStatusDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Worker para processar mensagens da fila RabbitMQ
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessamentoWorker {

    private final ExtratoServiceLocator serviceLocator;
    private final ProcessamentoStatusService statusService;
    private final RabbitTemplate rabbitTemplate;

    /**
     * Processa mensagens da fila de processamento
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_PROCESSAMENTO)
    public void processarArquivo(ProcessamentoMessage message) {
        UUID processamentoId = message.getProcessamentoId();
        
        try {
            log.info("Iniciando processamento: {} - {}", processamentoId, message.getNomeArquivo());
            
            // Atualizar status para processando
            atualizarStatusProcessando(processamentoId);
            
            // Processar arquivo com progresso real
            ProcessamentoResult resultado = processarArquivoComProgressoReal(processamentoId, message);
            
            // Marcar como concluído
            statusService.marcarConcluido(
                processamentoId,
                resultado.getTransacoesSalvas(),
                resultado.getTransacoesSalvas(),
                0, // duplicatas ignoradas - não disponível no ProcessamentoResult atual
                null // uploadId - não disponível no ProcessamentoResult atual
            );
            
            // Enviar notificação de status
            enviarNotificacaoStatus(processamentoId);
            
            log.info("Processamento concluído: {} - {} transações salvas", 
                    processamentoId, resultado.getTransacoesSalvas());
            
        } catch (Exception e) {
            log.error("Erro no processamento: {} - {}", processamentoId, e.getMessage(), e);
            
            // Marcar como erro
            statusService.marcarErro(processamentoId, e.getMessage());
            
            // Enviar notificação de erro
            enviarNotificacaoStatus(processamentoId);
        }
    }

    /**
     * Atualiza status para processando
     */
    private void atualizarStatusProcessando(UUID processamentoId) {
        ProcessamentoStatusDTO status = statusService.consultarStatus(processamentoId);
        if (status != null) {
            status.setStatus(ProcessamentoMessage.ProcessamentoStatus.PROCESSANDO);
            status.setMensagem("Processando arquivo...");
            statusService.atualizarStatus(status);
        }
    }

    /**
     * Processa arquivo com progresso real baseado no processamento efetivo
     */
    private ProcessamentoResult processarArquivoComProgressoReal(UUID processamentoId, ProcessamentoMessage message) {
        // Atualizar progresso inicial
        statusService.atualizarProgresso(processamentoId, 10, "Iniciando processamento...");
        enviarNotificacaoStatus(processamentoId);
        
        // Criar MockMultipartFile a partir do byte array
        org.springframework.mock.web.MockMultipartFile file = 
            new org.springframework.mock.web.MockMultipartFile(
                "file",
                message.getNomeArquivo(),
                "text/csv",
                message.getConteudoArquivo()
            );

        // Obter service do banco
        var service = serviceLocator.getService(message.getBanco());
        
        // Atualizar progresso antes do processamento
        statusService.atualizarProgresso(processamentoId, 30, "Processando arquivo...");
        enviarNotificacaoStatus(processamentoId);
        
        // Processar arquivo
        ProcessamentoResult resultado = service.processarArquivo(file);
        
        // Atualizar progresso final
        statusService.atualizarProgresso(processamentoId, 90, "Finalizando processamento...");
        enviarNotificacaoStatus(processamentoId);
        
        return resultado;
    }


    /**
     * Envia notificação de status via fila
     */
    private void enviarNotificacaoStatus(UUID processamentoId) {
        ProcessamentoStatusDTO status = statusService.consultarStatus(processamentoId);
        if (status != null) {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_PROCESSAMENTO,
                RabbitMQConfig.ROUTING_KEY_STATUS,
                status
            );
        }
    }
}
