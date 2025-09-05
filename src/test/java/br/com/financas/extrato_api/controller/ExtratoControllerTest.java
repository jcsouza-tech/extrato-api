package br.com.financas.extrato_api.controller;

import br.com.financas.extrato_api.exception.ExtratoExceptionControllerAdvice;
import br.com.financas.extrato_api.model.Transacao;
import br.com.financas.extrato_api.model.dto.TransacaoRepresentationAssembler;
import br.com.financas.extrato_api.service.ExtratoService;
import br.com.financas.extrato_api.service.ExtratoServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinancasController - Testes Unitários")
class ExtratoControllerTest {

    @Mock
    private ExtratoServiceLocator serviceLocator;
    
    @Mock
    private ExtratoService extratoService;
    
    @Mock
    private TransacaoRepresentationAssembler transacaoAssembler;

    @InjectMocks
    private ExtratoController financasController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(financasController)
                .setControllerAdvice(new ExtratoExceptionControllerAdvice())
                .build();
    }

    @Test
    @DisplayName("Deve carregar extrato com sucesso")
    void deveCarregarExtratoComSucesso() throws Exception {
        // Given
        String conteudoCSV = """
            Data,Lançamento,Detalhes,Número do Documento,Valor,Tipo do Lançamento
            15/01/2024,SAQUE,SAQUE 24H 001,123456,100.50,SAQUE
            """;
        
        MockMultipartFile arquivo = new MockMultipartFile(
            "file", 
            "extrato.csv", 
            "text/csv", 
            conteudoCSV.getBytes()
        );

        when(serviceLocator.getService("banco-do-brasil")).thenReturn(extratoService);
        when(extratoService.processarArquivo(any())).thenReturn(br.com.financas.extrato_api.model.dto.ProcessamentoResult.sucesso("extrato.csv", 0));

        // When & Then
        mockMvc.perform(multipart("/financas/carregar-extrato/banco-do-brasil")
                .file(arquivo))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.mensagem").value("Arquivo processado com sucesso"))
                .andExpect(jsonPath("$.transacoesSalvas").value(0));

        verify(extratoService, times(1)).processarArquivo(any());
    }

    @Test
    @DisplayName("Deve carregar extrato para banco específico")
    void deveCarregarExtratoParaBancoEspecifico() throws Exception {
        // Given
        String banco = "banco-do-brasil";
        String conteudoCSV = """
            Data,Lançamento,Detalhes,Número do Documento,Valor,Tipo do Lançamento
            15/01/2024,SAQUE,SAQUE 24H 001,123456,100.50,SAQUE
            """;
        
        MockMultipartFile arquivo = new MockMultipartFile(
            "file", 
            "extrato.csv", 
            "text/csv", 
            conteudoCSV.getBytes()
        );

        when(serviceLocator.getService(banco)).thenReturn(extratoService);
        when(extratoService.processarArquivo(any())).thenReturn(br.com.financas.extrato_api.model.dto.ProcessamentoResult.sucesso("extrato.csv", 0));

        // When & Then
        mockMvc.perform(multipart("/financas/carregar-extrato/{banco}", banco)
                .file(arquivo))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.mensagem").value("Arquivo processado com sucesso"))
                .andExpect(jsonPath("$.transacoesSalvas").value(0));

        verify(extratoService, times(1)).processarArquivo(any());
    }

    @Test
    @DisplayName("Deve visualizar extrato com transações")
    void deveVisualizarExtratoComTransacoes() throws Exception {
        // Given
        List<Transacao> transacoes = Arrays.asList(
            Transacao.builder()
                .id(1L)
                .data(LocalDate.of(2024, 1, 15))
                .lancamento("SAQUE")
                .detalhes("SAQUE 24H 001")
                .numeroDocumento("123456")
                .valor(new BigDecimal("100.50"))
                .tipoLancamento("SAQUE")
                .categoria("PENDENTE")
                .build(),
            Transacao.builder()
                .id(2L)
                .data(LocalDate.of(2024, 1, 16))
                .lancamento("DEPOSITO")
                .detalhes("DEPOSITO EM CONTA")
                .numeroDocumento("789012")
                .valor(new BigDecimal("500.00"))
                .tipoLancamento("DEPOSITO")
                .categoria("PENDENTE")
                .build()
        );

        when(serviceLocator.getService("banco-do-brasil")).thenReturn(extratoService);
        when(extratoService.getExtrato()).thenReturn(transacoes);
        when(transacaoAssembler.toCollectionModel(transacoes)).thenReturn(org.springframework.hateoas.CollectionModel.empty());

        // When & Then
        mockMvc.perform(get("/financas/visualisar-extrato"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.links").exists());

        verify(extratoService, times(1)).getExtrato();
    }

    @Test
    @DisplayName("Deve retornar erro quando arquivo não é fornecido")
    void deveRetornarErroQuandoArquivoNaoFornecido() throws Exception {
        // When & Then
        mockMvc.perform(multipart("/financas/carregar-extrato/banco-do-brasil"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro quando arquivo está vazio")
    void deveRetornarErroQuandoArquivoVazio() throws Exception {
        // Given
        when(serviceLocator.getService("banco-do-brasil")).thenReturn(extratoService);
        when(extratoService.processarArquivo(any()))
                .thenThrow(new br.com.financas.extrato_api.exception.ArquivoProcessamentoException("Arquivo está vazio"));
        
        MockMultipartFile arquivoVazio = new MockMultipartFile(
            "file", 
            "extrato-vazio.csv", 
            "text/csv", 
            new byte[0]
        );

        // When & Then
        mockMvc.perform(multipart("/financas/carregar-extrato/banco-do-brasil")
                .file(arquivoVazio))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro quando arquivo não é CSV")
    void deveRetornarErroQuandoArquivoNaoECSV() throws Exception {
        // Given
        when(serviceLocator.getService("banco-do-brasil")).thenReturn(extratoService);
        when(extratoService.processarArquivo(any()))
                .thenThrow(new br.com.financas.extrato_api.exception.FormatoArquivoInvalidoException("Arquivo deve ser CSV"));
        
        MockMultipartFile arquivoTexto = new MockMultipartFile(
            "file", 
            "extrato.txt", 
            "text/plain", 
            "Conteúdo de texto".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/financas/carregar-extrato/banco-do-brasil")
                .file(arquivoTexto))
                .andExpect(status().isUnsupportedMediaType());
    }
}
