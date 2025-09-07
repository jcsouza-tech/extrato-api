package br.com.financas.extrato_api.controller;

import br.com.financas.extrato_api.model.dto.ProcessamentoStatusDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

/**
 * Controller WebSocket para notificações de processamento
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ProcessamentoWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Envia notificação de status para todos os clientes conectados
     */
    public void enviarNotificacaoStatus(ProcessamentoStatusDTO status) {
        try {
            messagingTemplate.convertAndSend("/topic/processamento.status", status);
            log.debug("Notificação enviada: {} - {}", status.getProcessamentoId(), status.getStatus());
        } catch (Exception e) {
            log.error("Erro ao enviar notificação WebSocket: {}", e.getMessage(), e);
        }
    }

    /**
     * Envia notificação de status para um cliente específico
     */
    public void enviarNotificacaoStatus(UUID processamentoId, ProcessamentoStatusDTO status) {
        try {
            messagingTemplate.convertAndSendToUser(
                processamentoId.toString(),
                "/queue/processamento.status",
                status
            );
            log.debug("Notificação enviada para usuário: {} - {}", processamentoId, status.getStatus());
        } catch (Exception e) {
            log.error("Erro ao enviar notificação WebSocket para usuário: {}", e.getMessage(), e);
        }
    }

    /**
     * Envia notificação de progresso
     */
    public void enviarNotificacaoProgresso(UUID processamentoId, Integer progresso, String mensagem) {
        try {
            ProcessamentoStatusDTO status = ProcessamentoStatusDTO.builder()
                    .processamentoId(processamentoId)
                    .progresso(progresso)
                    .mensagem(mensagem)
                    .build();
            
            messagingTemplate.convertAndSend("/topic/processamento.progresso", status);
            log.debug("Notificação de progresso enviada: {} - {}%", processamentoId, progresso);
        } catch (Exception e) {
            log.error("Erro ao enviar notificação de progresso: {}", e.getMessage(), e);
        }
    }
}
