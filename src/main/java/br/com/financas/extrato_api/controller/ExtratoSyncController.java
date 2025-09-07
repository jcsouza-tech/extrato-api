package br.com.financas.extrato_api.controller;

import br.com.financas.extrato_api.model.Transacao;
import br.com.financas.extrato_api.model.dto.TransacaoDTO;
import br.com.financas.extrato_api.model.dto.TransacaoRepresentationAssembler;
import br.com.financas.extrato_api.model.dto.ProcessamentoResult;
import br.com.financas.extrato_api.model.dto.ProcessamentoStatusDTO;
import br.com.financas.extrato_api.service.ExtratoService;
import br.com.financas.extrato_api.service.ExtratoServiceLocator;
import br.com.financas.extrato_api.service.ProcessamentoAssincronoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/financas")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4000", "http://127.0.0.1:3000", "http://127.0.0.1:4000"})
@Tag(name = "Extrato", description = "API para processamento e visualização de extratos bancários")
public class ExtratoSyncController implements ExtratoController {

    private final ExtratoServiceLocator serviceLocator;
    private final TransacaoRepresentationAssembler transacaoAssembler;

    public ExtratoSyncController(ExtratoServiceLocator serviceLocator,
                                 TransacaoRepresentationAssembler transacaoAssembler) {
        this.serviceLocator = serviceLocator;
        this.transacaoAssembler = transacaoAssembler;
    }

    @Override
    @PostMapping(value="/carregar-extrato/{banco}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Upload de extrato bancário",
        description = "Faz upload e processa um arquivo CSV de extrato bancário para o banco especificado"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Arquivo processado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProcessamentoResult.class),
                examples = @ExampleObject(
                    value = """
                            {
                              "sucesso": true,
                              "mensagem": "Arquivo processado com sucesso",
                              "transacoesSalvas": 150,
                              "duplicatasIgnoradas": 5,
                              "uploadId": 123
                            }"""
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Arquivo inválido ou banco não suportado",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<ProcessamentoResult> carregarExtrato(
            @Parameter(description = "Nome do banco (ex: banco-do-brasil)", required = true)
            @PathVariable String banco,
            @Parameter(description = "Arquivo CSV do extrato bancário", required = true)
            @RequestParam("file") MultipartFile file) {
        ExtratoService service = serviceLocator.getService(banco);
        return ResponseEntity.ok().body(service.processarArquivo(file));
    }

    @GetMapping("/visualisar-extrato")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Visualizar extrato",
        description = "Lista todas as transações do extrato com paginação e links HATEOAS"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de transações retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CollectionModel.class),
                examples = @ExampleObject(
                    value = """
                            {
                              "content": [
                                {
                                  "id": 1,
                                  "data": "2024-01-15",
                                  "lancamento": "SAQUE",
                                  "detalhes": "SAQUE 24H 001",
                                  "numeroDocumento": "123456",
                                  "valor": 1234.56,
                                  "tipoLancamento": "SAQUE",
                                  "banco": "banco-do-brasil",
                                  "links": [
                                    {
                                      "rel": "self",
                                      "href": "http://localhost:8080/financas/visualisar-extrato/1"
                                    }
                                  ]
                                }
                              ],
                              "links": [
                                {
                                  "rel": "self",
                                  "href": "http://localhost:8080/financas/visualisar-extrato"
                                }
                              ]
                            }"""
                )
            )
        )
    })
    public CollectionModel<TransacaoDTO> visualizarExtrato() {
        // Usa o service padrão (Banco do Brasil)
        ExtratoService service = serviceLocator.getService("banco-do-brasil");
        List<Transacao> transacoes = service.getExtrato();
        return transacaoAssembler.toCollectionModel(transacoes);
    }
    
    /**
     * Endpoint para listar os bancos suportados
     * @return lista de bancos suportados
     */
    @GetMapping("/bancos-suportados")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Listar bancos suportados",
        description = "Retorna a lista de bancos suportados pela API"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de bancos retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "[\"banco-do-brasil\", \"itau\"]"
                )
            )
        )
    })
    public List<String> getBancosSuportados() {
        return serviceLocator.getBancosSuportados();
    }
}