package br.com.financas.extrato_api.controller;

import br.com.financas.extrato_api.model.dto.ProcessamentoStatusDTO;
import br.com.financas.extrato_api.service.ProcessamentoAssincronoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Controller dedicado para operações assíncronas de extrato via RabbitMQ
 */
@Slf4j
@RestController
@RequestMapping("/financas/rmq")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4000", "http://127.0.0.1:3000", "http://127.0.0.1:4000"})
@Tag(name = "Extrato RMQ", description = "Operações assíncronas de extrato via RabbitMQ")
public class ExtratoRMQController implements ExtratoController{

    private final ProcessamentoAssincronoService processamentoAssincronoService;

    /**
     * Carrega extrato de forma assíncrona via RabbitMQ
     */
    @PostMapping("/carregar-extrato-async/{banco}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
        summary = "Carregar extrato assíncrono",
        description = "Inicia o processamento assíncrono de um arquivo de extrato via RabbitMQ"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "202",
            description = "Processamento iniciado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProcessamentoStatusDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Arquivo inválido ou banco não suportado"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor"
        )
    })
    public ResponseEntity<ProcessamentoStatusDTO> carregarExtrato(
            @Parameter(description = "Banco para processamento", required = true)
            @PathVariable String banco,
            @Parameter(description = "Arquivo de extrato", required = true)
            @RequestParam("file") MultipartFile file) throws java.io.IOException {
        
        log.info("Iniciando processamento assíncrono para banco: {} - arquivo: {}", banco, file.getOriginalFilename());
        
        ProcessamentoStatusDTO status = processamentoAssincronoService.enviarParaProcessamento(
            banco, 
            file.getOriginalFilename(), 
            file.getBytes()
        );
        return ResponseEntity.accepted().body(status);
    }

    /**
     * Consulta status de processamento assíncrono
     */
    @GetMapping("/status/{processamentoId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Consultar status de processamento",
        description = "Retorna o status atual de um processamento assíncrono"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Status consultado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProcessamentoStatusDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Processamento não encontrado"
        )
    })
    public ResponseEntity<ProcessamentoStatusDTO> consultarStatus(
            @Parameter(description = "ID do processamento", required = true)
            @PathVariable UUID processamentoId) {
        
        log.info("Consultando status do processamento: {}", processamentoId);
        
        ProcessamentoStatusDTO status = processamentoAssincronoService.consultarStatus(processamentoId);
        return ResponseEntity.ok(status);
    }

    /**
     * Cancela processamento assíncrono
     */
    @PostMapping("/cancelar/{processamentoId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Cancelar processamento",
        description = "Cancela um processamento assíncrono em andamento"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Processamento cancelado com sucesso"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Processamento não encontrado"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Processamento não pode ser cancelado"
        )
    })
    public ResponseEntity<Void> cancelarProcessamento(
            @Parameter(description = "ID do processamento", required = true)
            @PathVariable UUID processamentoId) {
        
        log.info("Cancelando processamento: {}", processamentoId);
        
        processamentoAssincronoService.cancelarProcessamento(processamentoId);
        return ResponseEntity.ok().build();
    }

    /**
     * Lista todos os processamentos assíncronos
     */
    @GetMapping("/listar-processamentos")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Listar processamentos",
        description = "Retorna a lista de todos os processamentos assíncronos"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de processamentos retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProcessamentoStatusDTO.class)
            )
        )
    })
    public List<ProcessamentoStatusDTO> listarProcessamentos() {
        log.info("Listando todos os processamentos");
        return processamentoAssincronoService.listarProcessamentos();
    }

}
