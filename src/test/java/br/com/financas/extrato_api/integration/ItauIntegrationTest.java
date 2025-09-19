package br.com.financas.extrato_api.integration;

import br.com.financas.extrato_api.model.Transacao;
import br.com.financas.extrato_api.model.dto.ProcessamentoResult;
import br.com.financas.extrato_api.service.ExtratoServiceLocator;
import br.com.financas.extrato_api.service.ExtratoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Itaú - Teste de Integração")
class ItauIntegrationTest {

    @Autowired
    private ExtratoServiceLocator serviceLocator;

    @Test
    @DisplayName("Deve processar arquivo PDF do Itaú com sucesso")
    void deveProcessarArquivoPdfItau() throws IOException {
        // Given
        String pdfPath = "uploads/itau_extrato_052025.pdf";
        if (!Files.exists(Paths.get(pdfPath))) {
            // Se o arquivo não existir, pular o teste
            System.out.println("Arquivo PDF do Itaú não encontrado, pulando teste");
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
        ExtratoService itauService = serviceLocator.getService("itau");
        ProcessamentoResult result = itauService.processarArquivo(file);

        // Then
        assertNotNull(result);
        assertTrue(result.isSucesso());
        assertTrue(result.getTransacoesSalvas() > 0);
        assertEquals("itau_extrato_052025.pdf", result.getNomeArquivo());
        assertEquals("Arquivo processado com sucesso", result.getMensagem());

        System.out.println("Resultado do processamento:");
        System.out.println("- Arquivo: " + result.getNomeArquivo());
        System.out.println("- Sucesso: " + result.isSucesso());
        System.out.println("- Transações salvas: " + result.getTransacoesSalvas());
        System.out.println("- Mensagem: " + result.getMensagem());
    }

    @Test
    @DisplayName("Deve buscar extrato do Itaú após processamento")
    void deveBuscarExtratoItau() throws IOException {
        // Given
        String pdfPath = "uploads/itau_extrato_052025.pdf";
        if (!Files.exists(Paths.get(pdfPath))) {
            System.out.println("Arquivo PDF do Itaú não encontrado, pulando teste");
            return;
        }

        byte[] pdfContent = Files.readAllBytes(Paths.get(pdfPath));
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "itau_extrato_052025.pdf", 
            "application/pdf", 
            pdfContent
        );

        // Processar arquivo primeiro
        ExtratoService itauService = serviceLocator.getService("itau");
        ProcessamentoResult result = itauService.processarArquivo(file);
        assertTrue(result.isSucesso());

        // When
        List<Transacao> extrato = itauService.getExtrato();

        // Then
        assertNotNull(extrato);
        assertFalse(extrato.isEmpty());
        
        // Verificar se todas as transações são do Itaú
        for (Transacao transacao : extrato) {
            assertEquals("Itaú", transacao.getBanco());
            assertNotNull(transacao.getData());
            assertNotNull(transacao.getLancamento());
            assertNotNull(transacao.getValor());
            assertEquals("BRL", transacao.getMoeda());
        }

        System.out.println("Extrato do Itaú:");
        System.out.println("- Total de transações: " + extrato.size());
        
        // Mostrar algumas transações
        for (int i = 0; i < Math.min(5, extrato.size()); i++) {
            Transacao t = extrato.get(i);
            System.out.printf("- %s | %s | %s | %s%n", 
                t.getData(), t.getLancamento(), t.getValor(), t.getTipoLancamento());
        }
    }

    @Test
    @DisplayName("Deve detectar arquivo duplicado")
    void deveDetectarArquivoDuplicado() throws IOException {
        // Given
        String pdfPath = "uploads/itau_extrato_052025.pdf";
        if (!Files.exists(Paths.get(pdfPath))) {
            System.out.println("Arquivo PDF do Itaú não encontrado, pulando teste");
            return;
        }

        byte[] pdfContent = Files.readAllBytes(Paths.get(pdfPath));
        MockMultipartFile file1 = new MockMultipartFile(
            "file", 
            "itau_extrato_052025.pdf", 
            "application/pdf", 
            pdfContent
        );
        
        MockMultipartFile file2 = new MockMultipartFile(
            "file", 
            "itau_extrato_052025_copy.pdf", 
            "application/pdf", 
            pdfContent
        );

        ExtratoService itauService = serviceLocator.getService("itau");

        // When - Processar primeiro arquivo
        ProcessamentoResult result1 = itauService.processarArquivo(file1);
        assertTrue(result1.isSucesso());

        // When - Tentar processar arquivo duplicado
        ProcessamentoResult result2 = itauService.processarArquivo(file2);

        // Then
        assertNotNull(result2);
        assertFalse(result2.isSucesso());
        assertEquals("itau_extrato_052025_copy.pdf", result2.getNomeArquivo());
        assertEquals("Arquivo já foi processado anteriormente", result2.getMensagem());
        assertEquals(0, result2.getTransacoesSalvas());

        System.out.println("Teste de duplicata:");
        System.out.println("- Primeiro arquivo: " + result1.isSucesso() + " (" + result1.getTransacoesSalvas() + " transações)");
        System.out.println("- Arquivo duplicado: " + result2.isSucesso() + " (" + result2.getMensagem() + ")");
    }

    @Test
    @DisplayName("Deve verificar se Itaú está na lista de bancos suportados")
    void deveVerificarItauNaListaBancosSuportados() {
        // When
        List<String> bancosSuportados = serviceLocator.getBancosSuportados();

        // Then
        assertNotNull(bancosSuportados);
        assertTrue(bancosSuportados.contains("itau"));
        
        System.out.println("Bancos suportados: " + bancosSuportados);
    }
}
