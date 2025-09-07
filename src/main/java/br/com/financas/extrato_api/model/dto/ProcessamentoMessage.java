package br.com.financas.extrato_api.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para mensagens de processamento assíncrono
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessamentoMessage {
    
    private UUID processamentoId;
    private String banco;
    private String nomeArquivo;
    private byte[] conteudoArquivo;
    private String hashArquivo;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataEnvio;
    
    private String usuarioId;
    private String prioridade;
    
    // Status do processamento
    private ProcessamentoStatus status;
    
    public enum ProcessamentoStatus {
        PENDENTE,
        PROCESSANDO,
        CONCLUIDO,
        ERRO,
        CANCELADO
    }
}
