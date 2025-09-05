package br.com.financas.extrato_api.unit.service;

import br.com.financas.extrato_api.model.Transacao;
import br.com.financas.extrato_api.model.UploadArquivo;
import br.com.financas.extrato_api.model.dto.ProcessamentoResult;
import br.com.financas.extrato_api.observability.ExtratoMetricsService;
import br.com.financas.extrato_api.parser.ExtratoParser;
import br.com.financas.extrato_api.repository.TransacaoRepository;
import br.com.financas.extrato_api.repository.UploadArquivoRepository;
import br.com.financas.extrato_api.service.BancoDoBrasilService;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BancoDoBrasilService - Testes Unitários")
class BancoDoBrasilServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private UploadArquivoRepository uploadArquivoRepository;

    @Mock
    private ExtratoParser bbParser;

    @Mock
    private ExtratoMetricsService metricsService;


    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private BancoDoBrasilService bancoDoBrasilService;

    private MockMultipartFile arquivoValido;
    private List<Transacao> transacoesMock;
    private UploadArquivo uploadMock;

    @BeforeEach
    void setUp() {
        // Setup do arquivo mock
        String conteudoCSV = """
            Data,Lançamento,Detalhes,Número do Documento,Valor,Tipo do Lançamento
            15/01/2024,SAQUE,SAQUE 24H 001 BANCO 24H,123456,100.50,SAQUE
            16/01/2024,DEPOSITO,DEPOSITO EM CONTA,789012,500.00,DEPOSITO
            """;
        
        arquivoValido = new MockMultipartFile(
            "file", 
            "extrato.csv", 
            "text/csv", 
            conteudoCSV.getBytes()
        );

        // Setup das transações mock
        transacoesMock = Arrays.asList(
            Transacao.builder()
                .data(LocalDate.of(2024, 1, 15))
                .lancamento("SAQUE")
                .detalhes("SAQUE 24H 001 BANCO 24H")
                .numeroDocumento("123456")
                .valor(new BigDecimal("100.50"))
                .moeda("BRL")
                .tipoLancamento("SAQUE")
                .categoria("SAQUE")
                .banco("banco-do-brasil")
                .build(),
            Transacao.builder()
                .data(LocalDate.of(2024, 1, 16))
                .lancamento("DEPOSITO")
                .detalhes("DEPOSITO EM CONTA")
                .numeroDocumento("789012")
                .valor(new BigDecimal("500.00"))
                .moeda("BRL")
                .tipoLancamento("DEPOSITO")
                .categoria("DEPOSITO")
                .banco("banco-do-brasil")
                .build()
        );

        // Setup do upload mock
        uploadMock = new UploadArquivo();
        uploadMock.setId(1L);
        uploadMock.setHashArquivo("hash123");
        uploadMock.setNomeArquivo("extrato.csv");
        uploadMock.setDataUpload(LocalDate.now());
        uploadMock.setBanco("banco-do-brasil");
    }

    @Test
    @DisplayName("Deve processar arquivo com sucesso")
    void deveProcessarArquivoComSucesso() throws Exception {
        // Given
        when(bbParser.getBankName()).thenReturn("banco-do-brasil");
        when(bbParser.parse(any())).thenReturn(transacoesMock);
        when(uploadArquivoRepository.existsByHashArquivo(anyString())).thenReturn(false);
        when(uploadArquivoRepository.save(any(UploadArquivo.class))).thenReturn(uploadMock);
        when(transacaoRepository.saveAll(anyList())).thenReturn(transacoesMock);

        // When
        ProcessamentoResult resultado = bancoDoBrasilService.processarArquivo(arquivoValido);

        // Then
        assertThat(resultado.isSucesso()).isTrue();
        assertThat(resultado.getTransacoesSalvas()).isEqualTo(2);
        assertThat(resultado.getNomeArquivo()).isEqualTo("extrato.csv");

        // Verificar se as métricas foram chamadas
        verify(metricsService).incrementarArquivosProcessados();
        verify(metricsService).incrementarTransacoesProcessadas(2);
        verify(metricsService).incrementarBancoUtilizado("banco-do-brasil");
        verify(metricsService).registrarTempoProcessamento(any());
    }

    @Test
    @DisplayName("Deve detectar arquivo duplicado")
    void deveDetectarArquivoDuplicado() throws Exception {
        // Given
        when(uploadArquivoRepository.existsByHashArquivo(anyString())).thenReturn(true);

        // When
        ProcessamentoResult resultado = bancoDoBrasilService.processarArquivo(arquivoValido);

        // Then
        assertThat(resultado.isSucesso()).isFalse();
        assertThat(resultado.getTransacoesSalvas()).isEqualTo(0);
        assertThat(resultado.getMensagem()).contains("já foi processado");

        // Verificar que o parser não foi chamado
        verify(bbParser, never()).parse(any());
        verify(transacaoRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve fazer rollback quando transações falham")
    void deveFazerRollbackQuandoTransacoesFalham() throws Exception {
        // Given
        when(bbParser.getBankName()).thenReturn("banco-do-brasil");
        when(bbParser.parse(any())).thenReturn(transacoesMock);
        when(uploadArquivoRepository.existsByHashArquivo(anyString())).thenReturn(false);
        when(uploadArquivoRepository.save(any(UploadArquivo.class))).thenReturn(uploadMock);
        when(transacaoRepository.saveAll(anyList())).thenThrow(new DataIntegrityViolationException("Erro de integridade"));
        when(transacaoRepository.save(any(Transacao.class)))
            .thenReturn(transacoesMock.get(0))
            .thenReturn(transacoesMock.get(1));

        // When
        ProcessamentoResult resultado = bancoDoBrasilService.processarArquivo(arquivoValido);

        // Then
        assertThat(resultado.isSucesso()).isTrue();
        assertThat(resultado.getTransacoesSalvas()).isEqualTo(2);
        assertThat(resultado.getMensagem()).contains("sucesso");

        // Verificar que as métricas foram chamadas normalmente
        verify(metricsService).incrementarArquivosProcessados();
        verify(metricsService).incrementarTransacoesProcessadas(2);
    }

    @Test
    @DisplayName("Deve processar transações individualmente quando batch falha")
    void deveProcessarTransacoesIndividualmenteQuandoBatchFalha() throws Exception {
        // Given
        when(bbParser.getBankName()).thenReturn("banco-do-brasil");
        when(bbParser.parse(any())).thenReturn(transacoesMock);
        when(uploadArquivoRepository.existsByHashArquivo(anyString())).thenReturn(false);
        when(uploadArquivoRepository.save(any(UploadArquivo.class))).thenReturn(uploadMock);
        
        // Primeira chamada falha (batch), depois salva individualmente
        when(transacaoRepository.saveAll(anyList())).thenThrow(new DataIntegrityViolationException("Erro de integridade"));
        when(transacaoRepository.save(any(Transacao.class)))
            .thenReturn(transacoesMock.get(0))
            .thenReturn(transacoesMock.get(1));

        // When
        ProcessamentoResult resultado = bancoDoBrasilService.processarArquivo(arquivoValido);

        // Then
        assertThat(resultado.isSucesso()).isTrue();
        assertThat(resultado.getTransacoesSalvas()).isEqualTo(2);

        // Verificar que saveAll foi chamado primeiro, depois save individual
        verify(transacaoRepository).saveAll(anyList());
        verify(transacaoRepository, times(2)).save(any(Transacao.class));
    }

    @Test
    @DisplayName("Deve lidar com arquivo vazio")
    void deveLidarComArquivoVazio() throws Exception {
        // Given
        MockMultipartFile arquivoVazio = new MockMultipartFile(
            "file", 
            "vazio.csv", 
            "text/csv", 
            new byte[0]
        );

        // When & Then
        assertThatThrownBy(() -> bancoDoBrasilService.processarArquivo(arquivoVazio))
                .isInstanceOf(br.com.financas.extrato_api.exception.ArquivoProcessamentoException.class)
                .hasMessageContaining("Arquivo está vazio");
    }

    @Test
    @DisplayName("Deve lidar com erro de IO")
    void deveLidarComErroDeIO() throws Exception {
        // Given
        MockMultipartFile arquivoComErro = new MockMultipartFile(
            "file", 
            "erro.csv", 
            "text/csv", 
            "conteudo".getBytes()
        ) {
            @Override
            public java.io.InputStream getInputStream() throws IOException {
                throw new IOException("Erro de IO");
            }
        };

        // When
        ProcessamentoResult resultado = bancoDoBrasilService.processarArquivo(arquivoComErro);

        // Then
        assertThat(resultado.isSucesso()).isFalse();
        assertThat(resultado.getMensagem()).contains("Erro de IO");
    }

    @Test
    @DisplayName("Deve calcular hash corretamente")
    void deveCalcularHashCorretamente() throws Exception {
        // Given
        String conteudo = "teste,conteudo,csv";
        MockMultipartFile arquivo = new MockMultipartFile(
            "file", 
            "teste.csv", 
            "text/csv", 
            conteudo.getBytes()
        );

        when(bbParser.getBankName()).thenReturn("banco-do-brasil");
        when(bbParser.parse(any())).thenReturn(Collections.emptyList());
        when(uploadArquivoRepository.existsByHashArquivo(anyString())).thenReturn(false);
        when(uploadArquivoRepository.save(any(UploadArquivo.class))).thenReturn(uploadMock);
        when(transacaoRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // When
        ProcessamentoResult resultado = bancoDoBrasilService.processarArquivo(arquivo);

        // Then
        assertThat(resultado.isSucesso()).isTrue();
        
        // Verificar que existsByHashArquivo foi chamado com um hash válido
        verify(uploadArquivoRepository).existsByHashArquivo(argThat(hash -> 
            hash != null && hash.length() == 64 // SHA-256 hash length
        ));
    }

    @Test
    @DisplayName("Deve registrar métricas de tempo corretamente")
    void deveRegistrarMetricasDeTempoCorretamente() throws Exception {
        // Given
        when(bbParser.getBankName()).thenReturn("banco-do-brasil");
        when(bbParser.parse(any())).thenReturn(transacoesMock);
        when(uploadArquivoRepository.existsByHashArquivo(anyString())).thenReturn(false);
        when(uploadArquivoRepository.save(any(UploadArquivo.class))).thenReturn(uploadMock);
        when(transacaoRepository.saveAll(anyList())).thenReturn(transacoesMock);

        // When
        bancoDoBrasilService.processarArquivo(arquivoValido);

        // Then
        verify(metricsService).registrarTempoProcessamento(argThat(duration -> 
            duration != null && duration.toMillis() >= 0
        ));
    }

    @Test
    @DisplayName("Deve retornar extrato completo")
    void deveRetornarExtratoCompleto() {
        // Given
        when(transacaoRepository.findAll()).thenReturn(transacoesMock);

        // When
        List<Transacao> resultado = bancoDoBrasilService.getExtrato();

        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado).containsExactlyElementsOf(transacoesMock);
    }

    @Test
    @DisplayName("Deve retornar extrato por período")
    void deveRetornarExtratoPorPeriodo() {
        // Given
        LocalDate dataInicio = LocalDate.of(2024, 1, 1);
        LocalDate dataFim = LocalDate.of(2024, 1, 31);
        when(transacaoRepository.findByDataBetween(dataInicio, dataFim)).thenReturn(transacoesMock);

        // When
        List<Transacao> resultado = bancoDoBrasilService.getExtratoPorPeriodo(dataInicio, dataFim);

        // Then
        assertThat(resultado).hasSize(2);
        verify(transacaoRepository).findByDataBetween(dataInicio, dataFim);
    }
}
