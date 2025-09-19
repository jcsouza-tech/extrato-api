package br.com.financas.extrato_api.parser;

import br.com.financas.extrato_api.config.parser.ItauParserConfig;
import br.com.financas.extrato_api.model.Transacao;
import br.com.financas.extrato_api.model.parser.PdfConfig;
import br.com.financas.extrato_api.parser.validation.ItauValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItauParser - Testes Unitários")
class ItauParserTest {

    @Mock
    private ItauParserConfig config;
    
    @Mock
    private ItauValidation validation;
    
    @Mock
    private PdfConfig pdfConfig;

    @InjectMocks
    private ItauParser itauParser;

    @BeforeEach
    void setUp() {
        // Configuração dos mocks
        when(config.getPdf()).thenReturn(pdfConfig);
        when(pdfConfig.getDateFormat()).thenReturn("dd/MM/yyyy");
        when(config.getName()).thenReturn("Itaú");
        when(config.getSupportedExtensions()).thenReturn(List.of(".pdf"));
        when(config.getFilePatterns()).thenReturn(List.of(".*itau.*\\.pdf$"));
        
        // Mock da validação
        when(validation.isValidTransactionLine(any())).thenReturn(true);
        when(validation.extrairCamposTransacao(any())).thenReturn(new String[]{
            "21/08/2025", "PIX TRANSF MARYANN", "21/08", "-50,00"
        });
        when(validation.parseValorMonetario(any())).thenReturn(org.javamoney.moneta.Money.of(-50.00, "BRL"));
        when(validation.gerarNumeroDocumento(any())).thenReturn("123456");
        when(validation.determinarTipoLancamento(any(), any())).thenReturn("Saída");
    }

    @Test
    @DisplayName("Deve parsear linha de transação corretamente")
    void deveParsearLinhaTransacao() {
        // Given
        String linha = "21/08/2025 PIX TRANSF MARYANN21/08 -50,00";

        // When
        Optional<Transacao> transacao = itauParser.parseLine(linha);

        // Then
        assertTrue(transacao.isPresent());
        Transacao t = transacao.get();
        assertEquals("2025-08-21", t.getData().toString());
        assertEquals("PIX TRANSF MARYANN21/08", t.getLancamento());
        assertEquals("PIX TRANSF MARYANN21/08", t.getDetalhes());
        assertEquals(-50.00, t.getValor().doubleValue(), 0.01);
        assertEquals("BRL", t.getMoeda());
        assertEquals("Saída", t.getTipoLancamento());
        assertEquals("PENDENTE", t.getCategoria());
        assertEquals("Itaú", t.getBanco());
    }

    @Test
    @DisplayName("Deve parsear transação de entrada")
    void deveParsearTransacaoEntrada() {
        // Given
        String linha = "20/08/2025 TED 001.3652.JEAN C S D 4.203,46";

        // When
        Optional<Transacao> transacao = itauParser.parseLine(linha);

        // Then
        assertTrue(transacao.isPresent());
        Transacao t = transacao.get();
        assertEquals("2025-08-20", t.getData().toString());
        assertEquals("TED 001.3652.JEAN C S D", t.getLancamento());
        assertEquals(4203.46, t.getValor().doubleValue(), 0.01);
        assertEquals("Entrada", t.getTipoLancamento());
    }

    @Test
    @DisplayName("Deve ignorar linha de saldo")
    void deveIgnorarLinhaSaldo() {
        // Given
        String linha = "22/08/2025 SALDO DO DIA 2.178,23";

        // When
        Optional<Transacao> transacao = itauParser.parseLine(linha);

        // Then
        assertFalse(transacao.isPresent());
    }

    @Test
    @DisplayName("Deve ignorar linha vazia")
    void deveIgnorarLinhaVazia() {
        // Given
        String linha = "";

        // When
        Optional<Transacao> transacao = itauParser.parseLine(linha);

        // Then
        assertFalse(transacao.isPresent());
    }

    @Test
    @DisplayName("Deve ignorar linha nula")
    void deveIgnorarLinhaNula() {
        // Given
        String linha = null;

        // When
        Optional<Transacao> transacao = itauParser.parseLine(linha);

        // Then
        assertFalse(transacao.isPresent());
    }

    @Test
    @DisplayName("Deve parsear valor com milhares")
    void deveParsearValorComMilhares() {
        // Given
        String linha = "20/08/2025 PAG BOLETO GRPQA LTDA -1.230,53";

        // When
        Optional<Transacao> transacao = itauParser.parseLine(linha);

        // Then
        assertTrue(transacao.isPresent());
        Transacao t = transacao.get();
        assertEquals(-1230.53, t.getValor().doubleValue(), 0.01);
    }

    @Test
    @DisplayName("Deve gerar número de documento da descrição")
    void deveGerarNumeroDocumento() {
        // Given
        String linha = "20/08/2025 TED 001.3652.JEAN C S D 4.203,46";

        // When
        Optional<Transacao> transacao = itauParser.parseLine(linha);

        // Then
        assertTrue(transacao.isPresent());
        Transacao t = transacao.get();
        assertNotNull(t.getNumeroDocumento());
        assertFalse(t.getNumeroDocumento().isEmpty());
    }

    @Test
    @DisplayName("Deve retornar nome do banco correto")
    void deveRetornarNomeBanco() {
        // When
        String nomeBanco = itauParser.getBankName();

        // Then
        assertEquals("Itaú", nomeBanco);
    }

    @Test
    @DisplayName("Deve identificar arquivo do Itaú")
    void deveIdentificarArquivoItau() {
        // Given
        String fileName = "extrato_itau_052025.pdf";

        // When
        boolean supports = itauParser.supports(fileName);

        // Then
        assertTrue(supports);
    }

    @Test
    @DisplayName("Deve identificar arquivo PDF genérico")
    void deveIdentificarArquivoPdfGenerico() {
        // Given
        String fileName = "extrato.pdf";

        // When
        boolean supports = itauParser.supports(fileName);

        // Then
        assertTrue(supports);
    }

    @Test
    @DisplayName("Deve rejeitar arquivo não suportado")
    void deveRejeitarArquivoNaoSuportado() {
        // Given
        String fileName = "extrato_bb.csv";

        // When
        boolean supports = itauParser.supports(fileName);

        // Then
        assertFalse(supports);
    }

    @Test
    @DisplayName("Deve processar arquivo PDF real do Itaú")
    void deveProcessarArquivoPdfReal() throws IOException {
        // Given
        String pdfPath = "uploads/itau_extrato_052025.pdf";
        if (!Files.exists(Paths.get(pdfPath))) {
            // Se o arquivo não existir, pular o teste
            return;
        }

        byte[] pdfContent = Files.readAllBytes(Paths.get(pdfPath));
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "itau_extrato_052025.pdf", 
            "application/pdf", 
            pdfContent
        );

        // When
        List<Transacao> transacoes = itauParser.parse(file);

        // Then
        assertNotNull(transacoes);
        assertFalse(transacoes.isEmpty());
        
        // Verificar se as transações foram parseadas corretamente
        for (Transacao transacao : transacoes) {
            assertNotNull(transacao.getData());
            assertNotNull(transacao.getLancamento());
            assertNotNull(transacao.getValor());
            assertEquals("Itaú", transacao.getBanco());
            assertEquals("BRL", transacao.getMoeda());
            assertNotNull(transacao.getTipoLancamento());
            assertEquals("PENDENTE", transacao.getCategoria());
        }
        
        System.out.println("Transações parseadas: " + transacoes.size());
        for (int i = 0; i < Math.min(3, transacoes.size()); i++) {
            Transacao t = transacoes.get(i);
            System.out.printf("Transação %d: %s | %s | %s%n", 
                i + 1, t.getData(), t.getLancamento(), t.getValor());
        }
    }
}
