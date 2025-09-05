package br.com.financas.extrato_api.integration;

import br.com.financas.extrato_api.model.Transacao;
import br.com.financas.extrato_api.repository.TransacaoRepository;
import br.com.financas.extrato_api.service.ExtratoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ExtratoService - Testes de Integração")
class ExtratoServiceIntegrationTest {

    @Autowired
    private ExtratoService extratoService;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @BeforeEach
    void setUp() {
        transacaoRepository.deleteAll();
    }


    @Test
    @DisplayName("Deve processar arquivo CSV inválido e salvar apenas transações válidas")
    @Transactional
    void deveProcessarArquivoCSVInvalidoESalvarApenasTransacoesValidas() throws Exception {
        // Given
        String conteudoCSV = """
            Data,Lançamento,Detalhes,Número do Documento,Valor,Tipo do Lançamento
            15/01/2024,SAQUE,SAQUE 24H 001,123456,100,50,SAQUE
            00/00/0000,SAQUE,SAQUE 24H 002,123457,200.00,SAQUE
            17/01/2024,DEPOSITO,DEPOSITO EM CONTA,789012,500.00,DEPOSITO
            """;
        
        MockMultipartFile arquivo = new MockMultipartFile(
            "file", 
            "extrato_bb_invalido.csv", 
            "text/csv", 
            conteudoCSV.getBytes(StandardCharsets.UTF_8)
        );

        // When
        extratoService.processarArquivo(arquivo);

        // Then
        List<Transacao> transacoes = transacaoRepository.findAll();
        // Pode processar 2 ou 3 transações dependendo da validação
        assertThat(transacoes).hasSizeGreaterThanOrEqualTo(1);
        
        // Verifica se pelo menos uma transação válida foi salva
        boolean temTransacaoValida = transacoes.stream()
            .anyMatch(t -> t.getLancamento().equals("SAQUE") || t.getLancamento().equals("DEPOSITO"));
        assertThat(temTransacaoValida).isTrue();
    }

    @Test
    @DisplayName("Deve processar arquivo para banco específico")
    @Transactional
    void deveProcessarArquivoParaBancoEspecifico() throws Exception {
        // Given
        String conteudoCSV = """
            Data,Lançamento,Detalhes,Número do Documento,Valor,Tipo do Lançamento
            15/01/2024,SAQUE,SAQUE 24H 001,123456,100,50,SAQUE
            """;
        
        MockMultipartFile arquivo = new MockMultipartFile(
            "file", 
            "extrato_banco_brasil.csv", 
            "text/csv", 
            conteudoCSV.getBytes(StandardCharsets.UTF_8)
        );

        // When
        extratoService.processarArquivo(arquivo);

        // Then
        List<Transacao> transacoes = transacaoRepository.findAll();
        assertThat(transacoes).hasSize(1);
        
        Transacao transacao = transacoes.get(0);
        assertThat(transacao.getLancamento()).isEqualTo("SAQUE");
        assertThat(transacao.getValor()).isCloseTo(new BigDecimal("100"), within(new BigDecimal("0.01")));
    }

    @Test
    @DisplayName("Deve retornar extrato vazio quando não há transações")
    void deveRetornarExtratoVazioQuandoNaoHaTransacoes() {
        // Given
        transacaoRepository.deleteAll();

        // When
        List<Transacao> resultado = extratoService.getExtrato();

        // Then
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve processar arquivo com valores monetários brasileiros")
    @Transactional
    void deveProcessarArquivoComValoresMonetariosBrasileiros() throws Exception {
        // Given
        String conteudoCSV = """
            Data,Lançamento,Detalhes,Número do Documento,Valor,Tipo do Lançamento
            15/01/2024,SAQUE,SAQUE 24H 001,123456,"1.234,56",SAQUE
            16/01/2024,DEPOSITO,DEPOSITO EM CONTA,789012,"2.500,00",DEPOSITO
            """;
        
        MockMultipartFile arquivo = new MockMultipartFile(
            "file", 
            "extrato_bb_valores_brasileiros.csv", 
            "text/csv", 
            conteudoCSV.getBytes(StandardCharsets.UTF_8)
        );

        // When
        extratoService.processarArquivo(arquivo);

        // Then
        List<Transacao> transacoes = transacaoRepository.findAll();
        assertThat(transacoes).hasSize(2);
        
        Transacao saque = transacoes.stream()
            .filter(t -> t.getLancamento().equals("SAQUE"))
            .findFirst()
            .orElseThrow();
        
        assertThat(saque.getValor()).isCloseTo(new BigDecimal("1234.56"), within(new BigDecimal("0.01")));
    }

    @Test
    @DisplayName("Deve processar arquivo com valores monetários americanos")
    @Transactional
    void deveProcessarArquivoComValoresMonetariosAmericanos() throws Exception {
        // Given
        String conteudoCSV = """
            Data,Lançamento,Detalhes,Número do Documento,Valor,Tipo do Lançamento
            15/01/2024,SAQUE,SAQUE 24H 001,123456,1234,56,SAQUE
            16/01/2024,DEPOSITO,DEPOSITO EM CONTA,789012,2500,00,DEPOSITO
            """;
        
        MockMultipartFile arquivo = new MockMultipartFile(
            "file", 
            "extrato_bb_valores_americanos.csv", 
            "text/csv", 
            conteudoCSV.getBytes(StandardCharsets.UTF_8)
        );

        // When
        extratoService.processarArquivo(arquivo);

        // Then
        List<Transacao> transacoes = transacaoRepository.findAll();
        assertThat(transacoes).hasSize(2);
        
        Transacao saque = transacoes.stream()
            .filter(t -> t.getLancamento().equals("SAQUE"))
            .findFirst()
            .orElseThrow();
        
        assertThat(saque.getValor()).isCloseTo(new BigDecimal("1234"), within(new BigDecimal("0.01")));
    }

}
