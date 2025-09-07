package br.com.financas.extrato_api.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para status de processamento em tempo real
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessamentoStatusDTO {
    
    private UUID processamentoId;
    private String banco;
    private String nomeArquivo;
    private ProcessamentoMessage.ProcessamentoStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataInicio;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataFim;
    
    private Integer progresso; // 0-100
    private String mensagem;
    private String erro;
    
    // Resultados do processamento
    private Integer transacoesProcessadas;
    private Integer transacoesSalvas;
    private Integer duplicatasIgnoradas;
    private Long uploadId;
    
    // Métricas de performance
    private Long tempoProcessamentoMs;
    private Double velocidadeProcessamento; // transações/segundo
}
