package br.com.financas.extrato_api.parser;

import br.com.financas.extrato_api.config.parser.BancoDoBrasilParserConfig;
import br.com.financas.extrato_api.config.parser.BankParserConfig;
import br.com.financas.extrato_api.model.Transacao;
import br.com.financas.extrato_api.model.parser.CsvConfig;
import br.com.financas.extrato_api.parser.validation.BBValidation;
import br.com.financas.extrato_api.parser.validation.Validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@DisplayName("BancoDoBrasilParser - Testes Unitários")
@ExtendWith(MockitoExtension.class)
class BancoDoBrasilParserTest {

    @Mock
    private BancoDoBrasilParserConfig config;

    @Mock
    private CsvConfig csvConfig;
    private BBValidation validation;

    private BancoDoBrasilParser parser;

    private DateTimeFormatter dateFormatter;

    @BeforeEach
    void setUp() {
        // Configurar mocks com lenient() para evitar UnnecessaryStubbingException
        lenient().when(csvConfig.getDateFormat()).thenReturn("dd/MM/yyyy");
        lenient().when(csvConfig.getSeparator()).thenReturn(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        lenient().when(csvConfig.getSkipLine()).thenReturn(1);
        lenient().when(csvConfig.getDateRegex()).thenReturn("^\\d{2}/\\d{2}/\\d{4}$");
        lenient().when(csvConfig.getValueRegex()).thenReturn("^[+-]?\\d{1,3}([.,]\\d{3})*([.,]\\d{1,2})?$|^[+-]?\\d+([.,]\\d{1,2})?$");
        lenient().when(csvConfig.getIdioma()).thenReturn("pt");
        lenient().when(csvConfig.getPais()).thenReturn("BR");
        lenient().when(config.getName()).thenReturn("Banco do Brasil");
        lenient().when(config.getSupportedExtensions()).thenReturn(List.of(".csv"));
        lenient().when(config.getFilePatterns()).thenReturn(List.of(".*extrato.*bb.*", ".*bb.*extrato.*",".*bb.*\\.csv$"));
        lenient().when(config.getCsv()).thenReturn(csvConfig);
        
        // Criar instância real do BBValidation
        validation = new BBValidation(config);

        // Inicializar o parser manualmente (já que não usamos Spring no teste)
        dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        parser = new BancoDoBrasilParser(config, validation);
    }

    @Test
    @DisplayName("Deve suportar Banco do Brasil")
    void deveSuportarBancoDoBrasil() {
        // When & Then
        assertThat(parser.supports("extrato_bb.csv")).isTrue();
        assertThat(parser.supports("banco_bb.csv")).isTrue();
        assertThat(parser.supports("bb_extrato.csv")).isTrue();
        assertThat(parser.supports("itau_extrato.csv")).isFalse();
        assertThat(parser.supports("santander.csv")).isFalse();
    }

    @Test
    @DisplayName("Deve retornar nome do banco correto")
    void deveRetornarNomeDoBancoCorreto() {
        // When & Then
        assertThat(parser.getBankName()).isEqualTo("Banco do Brasil");
    }

    @Test
    @DisplayName("Deve processar arquivo CSV válido")
    void deveProcessarArquivoCSVValido() throws Exception {
        // Given - Baseado no arquivo real do Banco do Brasil
        String conteudoCSV = """
            "Data","Lançamento","Detalhes","Nº documento","Valor","Tipo Lançamento"
            "02/05/2025","Compra com Cartão","01/05 20:08 COMERCIAL DE ALIMENT","643752","-41,66","Saída"
            "02/05/2025","BB Rende Fácil","Rende Facil","9903","42,15","Entrada"
            """;
        
        MockMultipartFile arquivo = new MockMultipartFile(
            "file", 
            "extrato_bb.csv", 
            "text/csv", 
            conteudoCSV.getBytes(StandardCharsets.ISO_8859_1)
        );

        // When
        List<Transacao> transacoes = parser.parse(arquivo);

        // Debug: Vamos ver o que está acontecendo
        System.out.println("Transações encontradas: " + transacoes.size());
        for (int i = 0; i < transacoes.size(); i++) {
            Transacao t = transacoes.get(i);
            System.out.println("Transação " + i + ": " + t);
            if (t != null) {
                System.out.println("  - Lançamento: " + t.getLancamento());
                System.out.println("  - Tipo: " + t.getTipoLancamento());
                System.out.println("  - Valor: " + t.getValor());
            }
        }

        // Then
        assertThat(transacoes).hasSize(2);
        
        Transacao primeira = transacoes.get(0);
        // Simula o comportamento do prePersist() para o teste
        if (primeira.getValorMonetario() != null) {
            primeira.setValor(primeira.getValorMonetario().getNumber().numberValue(BigDecimal.class));
            primeira.setMoeda(primeira.getValorMonetario().getCurrency().getCurrencyCode());
        }
        assertThat(primeira.getLancamento()).isEqualTo("Compra com Cartão");
        assertThat(primeira.getDetalhes()).isEqualTo("01/05 20:08 COMERCIAL DE ALIMENT");
        assertThat(primeira.getNumeroDocumento()).isEqualTo("643752");
        assertThat(primeira.getTipoLancamento()).isEqualTo("Saída");
        assertThat(primeira.getCategoria()).isEqualTo("PENDENTE");
        assertThat(primeira.getData()).isEqualTo(LocalDate.of(2025, 5, 2));
        assertThat(primeira.getValor()).isCloseTo(new BigDecimal("-41.66"), within(new BigDecimal("0.01")));
        
        Transacao segunda = transacoes.get(1);
        // Simula o comportamento do prePersist() para o teste
        if (segunda.getValorMonetario() != null) {
            segunda.setValor(segunda.getValorMonetario().getNumber().numberValue(BigDecimal.class));
            segunda.setMoeda(segunda.getValorMonetario().getCurrency().getCurrencyCode());
        }
        assertThat(segunda.getLancamento()).isEqualTo("BB Rende Fácil");
        assertThat(segunda.getDetalhes()).isEqualTo("Rende Facil");
        assertThat(segunda.getNumeroDocumento()).isEqualTo("9903");
        assertThat(segunda.getTipoLancamento()).isEqualTo("Entrada");
        assertThat(segunda.getCategoria()).isEqualTo("PENDENTE");
        assertThat(segunda.getData()).isEqualTo(LocalDate.of(2025, 5, 2));
        assertThat(segunda.getValor()).isCloseTo(new BigDecimal("42.15"), within(new BigDecimal("0.01")));
    }

    @Test
    @DisplayName("Deve processar arquivo com valores monetários brasileiros")
    void deveProcessarArquivoComValoresMonetariosBrasileiros() throws Exception {
        // Given
        String conteudoCSV = """
            Data,Lançamento,Detalhes,Número do Documento,Valor,Tipo do Lançamento
            "15/01/2024","SAQUE","SAQUE 24H 001","123456","1.234,56","SAQUE"
            "16/01/2024","DEPOSITO","DEPOSITO EM CONTA","789012","2.500,00","DEPOSITO"
            """;
        
        MockMultipartFile arquivo = new MockMultipartFile(
            "file", 
            "extrato_bb.csv",
            "text/csv", 
            conteudoCSV.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<Transacao> transacoes = parser.parse(arquivo);

        // Then
        assertThat(transacoes).hasSize(2);
        
        Transacao saque = transacoes.get(0);
        // Simula o comportamento do prePersist() para o teste
        if (saque.getValorMonetario() != null) {
            saque.setValor(saque.getValorMonetario().getNumber().numberValue(BigDecimal.class));
            saque.setMoeda(saque.getValorMonetario().getCurrency().getCurrencyCode());
        }
        assertThat(saque.getLancamento()).isEqualTo("SAQUE");
        assertThat(saque.getValor()).isCloseTo(new BigDecimal("1234.56"), within(new BigDecimal("0.01")));
        
        Transacao deposito = transacoes.get(1);
        // Simula o comportamento do prePersist() para o teste
        if (deposito.getValorMonetario() != null) {
            deposito.setValor(deposito.getValorMonetario().getNumber().numberValue(BigDecimal.class));
            deposito.setMoeda(deposito.getValorMonetario().getCurrency().getCurrencyCode());
        }
        assertThat(deposito.getLancamento()).isEqualTo("DEPOSITO");
        assertThat(deposito.getValor()).isCloseTo(new BigDecimal("2500.00"), within(new BigDecimal("0.01")));
    }

    @Test
    @DisplayName("Deve processar arquivo com valores monetários americanos")
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
        List<Transacao> transacoes = parser.parse(arquivo);

        // Then
        assertThat(transacoes).hasSize(2);
        
        Transacao saque = transacoes.get(0);
        // Simula o comportamento do prePersist() para o teste
        if (saque.getValorMonetario() != null) {
            saque.setValor(saque.getValorMonetario().getNumber().numberValue(BigDecimal.class));
            saque.setMoeda(saque.getValorMonetario().getCurrency().getCurrencyCode());
        }
        assertThat(saque.getLancamento()).isEqualTo("SAQUE");
        assertThat(saque.getValor()).isCloseTo(new BigDecimal("1234"), within(new BigDecimal("0.01")));
        
        Transacao deposito = transacoes.get(1);
        // Simula o comportamento do prePersist() para o teste
        if (deposito.getValorMonetario() != null) {
            deposito.setValor(deposito.getValorMonetario().getNumber().numberValue(BigDecimal.class));
            deposito.setMoeda(deposito.getValorMonetario().getCurrency().getCurrencyCode());
        }
        assertThat(deposito.getLancamento()).isEqualTo("DEPOSITO");
        assertThat(deposito.getValor()).isCloseTo(new BigDecimal("2500.00"), within(new BigDecimal("0.01")));
    }

    @Test
    @DisplayName("Deve processar arquivo com caracteres especiais")
    void deveProcessarArquivoComCaracteresEspeciais() throws Exception {
        // Given
        String conteudoCSV = """
            Data,Lançamento,Detalhes,Número do Documento,Valor,Tipo do Lançamento
            15/01/2024,SAQUE,SAQUE 24H 001 - Açúcar & Café,123456,100,50,SAQUE
            """;
        
        MockMultipartFile arquivo = new MockMultipartFile(
            "file", 
            "extrato_bb_caracteres_especiais.csv", 
            "text/csv", 
            conteudoCSV.getBytes(StandardCharsets.ISO_8859_1)
        );

        // When
        List<Transacao> transacoes = parser.parse(arquivo);

        // Then
        assertThat(transacoes).hasSize(1);
        
        Transacao transacao = transacoes.get(0);
        assertThat(transacao.getDetalhes()).contains("Açúcar & Café");
    }

    @Test
    @DisplayName("Deve processar arquivo com campos vazios")
    void deveProcessarArquivoComCamposVazios() throws Exception {
        // Given
        String conteudoCSV = """
            Data,Lançamento,Detalhes,Número do Documento,Valor,Tipo do Lançamento
            15/01/2024,SAQUE,,123456,100,50,SAQUE
            16/01/2024,DEPOSITO,DEPOSITO EM CONTA,,500,00,DEPOSITO
            """;
        
        MockMultipartFile arquivo = new MockMultipartFile(
            "file", 
            "extrato_bb_campos_vazios.csv", 
            "text/csv", 
            conteudoCSV.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<Transacao> transacoes = parser.parse(arquivo);

        // Then
        assertThat(transacoes).hasSize(2);
        
        Transacao saque = transacoes.get(0);
        assertThat(saque.getDetalhes()).isEmpty();
        
        Transacao deposito = transacoes.get(1);
        assertThat(deposito.getNumeroDocumento()).isEmpty();
    }

    @Test
    @DisplayName("Deve processar arquivo com valores negativos")
    void deveProcessarArquivoComValoresNegativos() throws Exception {
        // Given
        String conteudoCSV = """
            Data,Lançamento,Detalhes,Número do Documento,Valor,Tipo do Lançamento
            15/01/2024,SAQUE,SAQUE 24H 001,123456,-100,50,SAQUE
            16/01/2024,COMPRA,COMPRA COM CARTAO,789012,-250,00,COMPRA
            """;
        
        MockMultipartFile arquivo = new MockMultipartFile(
            "file", 
            "extrato_bb_valores_negativos.csv", 
            "text/csv", 
            conteudoCSV.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<Transacao> transacoes = parser.parse(arquivo);

        // Then
        assertThat(transacoes).hasSize(2);
        
        Transacao saque = transacoes.get(0);
        // Simula o comportamento do prePersist() para o teste
        if (saque.getValorMonetario() != null) {
            saque.setValor(saque.getValorMonetario().getNumber().numberValue(BigDecimal.class));
            saque.setMoeda(saque.getValorMonetario().getCurrency().getCurrencyCode());
        }
        assertThat(saque.getValor()).isCloseTo(new BigDecimal("-100"), within(new BigDecimal("0.01")));
        
        Transacao compra = transacoes.get(1);
        // Simula o comportamento do prePersist() para o teste
        if (compra.getValorMonetario() != null) {
            compra.setValor(compra.getValorMonetario().getNumber().numberValue(BigDecimal.class));
            compra.setMoeda(compra.getValorMonetario().getCurrency().getCurrencyCode());
        }
        assertThat(compra.getValor()).isCloseTo(new BigDecimal("-250.00"), within(new BigDecimal("0.01")));
    }

    @Test
    @DisplayName("Deve processar arquivo vazio (apenas cabeçalho)")
    void deveProcessarArquivoVazioApenasCabecalho() throws Exception {
        // Given
        String conteudoCSV = "Data,Lançamento,Detalhes,Número do Documento,Valor,Tipo do Lançamento\n";
        
        MockMultipartFile arquivo = new MockMultipartFile(
            "file", 
            "extrato_vazio.csv", 
            "text/csv", 
            conteudoCSV.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<Transacao> transacoes = parser.parse(arquivo);

        // Then
        assertThat(transacoes).isEmpty();
    }

    @Test
    @DisplayName("Deve processar arquivo com linhas inválidas")
    void deveProcessarArquivoComLinhasInvalidas() throws Exception {
        // Given
        String conteudoCSV = """
            Data,Lançamento,Detalhes,Número do Documento,Valor,Tipo do Lançamento
            15/01/2024,SAQUE,SAQUE 24H 001,123456,100,50,SAQUE
            linha inválida sem vírgulas
            16/01/2024,DEPOSITO,DEPOSITO EM CONTA,789012,500,00,DEPOSITO
            """;
        
        MockMultipartFile arquivo = new MockMultipartFile(
            "file", 
            "extrato_bb_linhas_invalidas.csv", 
            "text/csv", 
            conteudoCSV.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<Transacao> transacoes = parser.parse(arquivo);

        // Then
        assertThat(transacoes).hasSize(2);
        
        Transacao saque = transacoes.get(0);
        assertThat(saque.getLancamento()).isEqualTo("SAQUE");
        
        Transacao deposito = transacoes.get(1);
        assertThat(deposito.getLancamento()).isEqualTo("DEPOSITO");
    }

    @Test
    @DisplayName("Deve processar arquivo com aspas")
    void deveProcessarArquivoComAspas() throws Exception {
        // Given
        String conteudoCSV = """
            "Data","Lançamento","Detalhes","Número do Documento","Valor","Tipo do Lançamento"
            "15/01/2024","SAQUE","SAQUE 24H 001","123456","100,50","SAQUE"
            "16/01/2024","DEPOSITO","DEPOSITO EM CONTA","789012","500,00","DEPOSITO"
            """;
        
        MockMultipartFile arquivo = new MockMultipartFile(
            "file", 
            "extrato_bb_com_aspas.csv", 
            "text/csv", 
            conteudoCSV.getBytes(StandardCharsets.UTF_8)
        );

        // When
        List<Transacao> transacoes = parser.parse(arquivo);

        // Then
        assertThat(transacoes).hasSize(2);
        
        Transacao saque = transacoes.get(0);
        assertThat(saque.getLancamento()).isEqualTo("SAQUE");
        assertThat(saque.getDetalhes()).isEqualTo("SAQUE 24H 001");
        
        Transacao deposito = transacoes.get(1);
        assertThat(deposito.getLancamento()).isEqualTo("DEPOSITO");
        assertThat(deposito.getDetalhes()).isEqualTo("DEPOSITO EM CONTA");
    }
}
