package br.com.financas.extrato_api.controller;

import br.com.financas.extrato_api.model.Transacao;
import br.com.financas.extrato_api.model.dto.TransacaoHateoasDTO;
import br.com.financas.extrato_api.model.dto.TransacaoRepresentationAssembler;
import br.com.financas.extrato_api.model.dto.ProcessamentoResult;
import br.com.financas.extrato_api.service.ExtratoService;
import br.com.financas.extrato_api.service.ExtratoServiceLocator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/financas")
@Tag(name = "Extrato", description = "API para processamento e visualização de extratos bancários")
public class ExtratoController {

    private final ExtratoServiceLocator serviceLocator;
    private final TransacaoRepresentationAssembler transacaoAssembler;

    @Autowired
    public ExtratoController(ExtratoServiceLocator serviceLocator,
                           TransacaoRepresentationAssembler transacaoAssembler) {
        this.serviceLocator = serviceLocator;
        this.transacaoAssembler = transacaoAssembler;
    }

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
                    value = "{\n" +
                            "  \"sucesso\": true,\n" +
                            "  \"mensagem\": \"Arquivo processado com sucesso\",\n" +
                            "  \"transacoesSalvas\": 150,\n" +
                            "  \"duplicatasIgnoradas\": 5,\n" +
                            "  \"uploadId\": 123\n" +
                            "}"
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
    public ProcessamentoResult carregarExtrato(
            @Parameter(description = "Nome do banco (ex: banco-do-brasil)", required = true)
            @PathVariable String banco, 
            @Parameter(description = "Arquivo CSV do extrato bancário", required = true)
            @RequestParam("file") MultipartFile file) {
        ExtratoService service = serviceLocator.getService(banco);
        return service.processarArquivo(file);
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
                    value = "{\n" +
                            "  \"content\": [\n" +
                            "    {\n" +
                            "      \"id\": 1,\n" +
                            "      \"data\": \"2024-01-15\",\n" +
                            "      \"lancamento\": \"SAQUE\",\n" +
                            "      \"detalhes\": \"SAQUE 24H 001\",\n" +
                            "      \"numeroDocumento\": \"123456\",\n" +
                            "      \"valor\": 1234.56,\n" +
                            "      \"tipoLancamento\": \"SAQUE\",\n" +
                            "      \"banco\": \"banco-do-brasil\",\n" +
                            "      \"links\": [\n" +
                            "        {\n" +
                            "          \"rel\": \"self\",\n" +
                            "          \"href\": \"http://localhost:8080/financas/visualisar-extrato/1\"\n" +
                            "        }\n" +
                            "      ]\n" +
                            "    }\n" +
                            "  ],\n" +
                            "  \"links\": [\n" +
                            "    {\n" +
                            "      \"rel\": \"self\",\n" +
                            "      \"href\": \"http://localhost:8080/financas/visualisar-extrato\"\n" +
                            "    }\n" +
                            "  ]\n" +
                            "}"
                )
            )
        )
    })
    public CollectionModel<TransacaoHateoasDTO> visualizarExtrato() {
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