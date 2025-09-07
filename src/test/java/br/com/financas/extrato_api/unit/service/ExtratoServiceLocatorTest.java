package br.com.financas.extrato_api.unit.service;

import br.com.financas.extrato_api.config.BancosSuportadosConfig;
import br.com.financas.extrato_api.exception.BancoNaoSuportadoException;
import br.com.financas.extrato_api.service.ExtratoService;
import br.com.financas.extrato_api.service.ExtratoServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExtratoServiceLocator - Testes Unitários")
class ExtratoServiceLocatorTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private BancosSuportadosConfig bancosConfig;

    @Mock
    private ExtratoService extratoService;

    private ExtratoServiceLocator serviceLocator;

    @BeforeEach
    void setUp() {
        serviceLocator = new ExtratoServiceLocator(applicationContext, bancosConfig);
    }

    @Test
    @DisplayName("Deve retornar service quando banco é suportado")
    void deveRetornarServiceQuandoBancoESuportado() {
        // Given
        String nomeBanco = "banco-do-brasil";
        List<String> bancosSuportados = Arrays.asList("banco-do-brasil", "itau");
        
        when(bancosConfig.getNames()).thenReturn(bancosSuportados);
        when(applicationContext.getBean("banco-do-brasil-service", ExtratoService.class))
            .thenReturn(extratoService);

        // When
        ExtratoService resultado = serviceLocator.getService(nomeBanco);

        // Then
        assertThat(resultado).isEqualTo(extratoService);
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome do banco é null")
    void deveLancarExcecaoQuandoNomeDoBancoENull() {
        // When & Then
        assertThatThrownBy(() -> serviceLocator.getService(null))
            .isInstanceOf(BancoNaoSuportadoException.class)
            .hasMessage("Banco não suportado: Nome do banco não pode ser vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome do banco é vazio")
    void deveLancarExcecaoQuandoNomeDoBancoEVazio() {
        // When & Then
        assertThatThrownBy(() -> serviceLocator.getService(""))
            .isInstanceOf(BancoNaoSuportadoException.class)
            .hasMessage("Banco não suportado: Nome do banco não pode ser vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome do banco é apenas espaços")
    void deveLancarExcecaoQuandoNomeDoBancoESoEspacos() {
        // When & Then
        assertThatThrownBy(() -> serviceLocator.getService("   "))
            .isInstanceOf(BancoNaoSuportadoException.class)
            .hasMessage("Banco não suportado: Nome do banco não pode ser vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando banco não é suportado")
    void deveLancarExcecaoQuandoBancoNaoESuportado() {
        // Given
        String nomeBanco = "banco-inexistente";
        List<String> bancosSuportados = Arrays.asList("banco-do-brasil", "itau");
        
        when(bancosConfig.getNames()).thenReturn(bancosSuportados);

        // When & Then
        assertThatThrownBy(() -> serviceLocator.getService(nomeBanco))
            .isInstanceOf(BancoNaoSuportadoException.class)
            .hasMessage("Banco não suportado: banco-inexistente");
    }

    @Test
    @DisplayName("Deve lançar exceção quando service não é encontrado no contexto")
    void deveLancarExcecaoQuandoServiceNaoEncontrado() {
        // Given
        String nomeBanco = "banco-do-brasil";
        List<String> bancosSuportados = Arrays.asList("banco-do-brasil");
        
        when(bancosConfig.getNames()).thenReturn(bancosSuportados);
        when(applicationContext.getBean("banco-do-brasil-service", ExtratoService.class))
            .thenThrow(new RuntimeException("Bean não encontrado"));

        // When & Then
        assertThatThrownBy(() -> serviceLocator.getService(nomeBanco))
            .isInstanceOf(BancoNaoSuportadoException.class)
            .hasMessage("Banco não suportado: Service não encontrado para banco: banco-do-brasil (bean: banco-do-brasil-service)");
    }

    @Test
    @DisplayName("Deve retornar lista de bancos suportados")
    void deveRetornarListaDeBancosSuportados() {
        // Given
        List<String> bancosSuportados = Arrays.asList("banco-do-brasil", "itau", "bradesco");
        when(bancosConfig.getNames()).thenReturn(bancosSuportados);

        // When
        List<String> resultado = serviceLocator.getBancosSuportados();

        // Then
        assertThat(resultado).isEqualTo(bancosSuportados);
    }

    @Test
    @DisplayName("Deve normalizar nome do banco (lowercase e trim)")
    void deveNormalizarNomeDoBanco() {
        // Given
        String nomeBanco = "  BANCO-DO-BRASIL  ";
        List<String> bancosSuportados = Arrays.asList("banco-do-brasil");
        
        when(bancosConfig.getNames()).thenReturn(bancosSuportados);
        when(applicationContext.getBean("banco-do-brasil-service", ExtratoService.class))
            .thenReturn(extratoService);

        // When
        ExtratoService resultado = serviceLocator.getService(nomeBanco);

        // Then
        assertThat(resultado).isEqualTo(extratoService);
    }
}
