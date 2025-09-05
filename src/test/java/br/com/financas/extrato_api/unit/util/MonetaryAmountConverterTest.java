package br.com.financas.extrato_api.unit.util;

import br.com.financas.extrato_api.util.MonetaryAmountConverter;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MonetaryAmountConverter - Testes Unitários")
class MonetaryAmountConverterTest {

    private final MonetaryAmountConverter converter = new MonetaryAmountConverter();

    @Test
    @DisplayName("Deve converter MonetaryAmount para String")
    void deveConverterMonetaryAmountParaString() {
        // Given
        MonetaryAmount amount = Money.of(new BigDecimal("100.50"), "BRL");

        // When
        String resultado = converter.convertToDatabaseColumn(amount);

        // Then
        assertThat(resultado).isEqualTo("100.5 BRL");
    }

    @Test
    @DisplayName("Deve converter String para MonetaryAmount")
    void deveConverterStringParaMonetaryAmount() {
        // Given
        String dbData = "100.50 BRL";

        // When
        MonetaryAmount resultado = converter.convertToEntityAttribute(dbData);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNumber().numberValue(BigDecimal.class)).isEqualTo(new BigDecimal("100.5"));
        assertThat(resultado.getCurrency().getCurrencyCode()).isEqualTo("BRL");
    }

    @Test
    @DisplayName("Deve converter valor zero")
    void deveConverterValorZero() {
        // Given
        MonetaryAmount amount = Money.of(BigDecimal.ZERO, "BRL");

        // When
        String resultado = converter.convertToDatabaseColumn(amount);

        // Then
        assertThat(resultado).isEqualTo("0 BRL");
    }

    @Test
    @DisplayName("Deve converter valor negativo")
    void deveConverterValorNegativo() {
        // Given
        MonetaryAmount amount = Money.of(new BigDecimal("-100.50"), "BRL");

        // When
        String resultado = converter.convertToDatabaseColumn(amount);

        // Then
        assertThat(resultado).isEqualTo("-100.5 BRL");
    }

    @Test
    @DisplayName("Deve converter valor com diferentes moedas")
    void deveConverterValorComDiferentesMoedas() {
        // Given
        MonetaryAmount amountUSD = Money.of(new BigDecimal("100.50"), "USD");
        MonetaryAmount amountEUR = Money.of(new BigDecimal("100.50"), "EUR");

        // When
        String resultadoUSD = converter.convertToDatabaseColumn(amountUSD);
        String resultadoEUR = converter.convertToDatabaseColumn(amountEUR);

        // Then
        assertThat(resultadoUSD).isEqualTo("100.5 USD");
        assertThat(resultadoEUR).isEqualTo("100.5 EUR");
    }

    @Test
    @DisplayName("Deve converter String com diferentes moedas")
    void deveConverterStringComDiferentesMoedas() {
        // Given
        String dbDataUSD = "100.50 USD";
        String dbDataEUR = "100.50 EUR";

        // When
        MonetaryAmount resultadoUSD = converter.convertToEntityAttribute(dbDataUSD);
        MonetaryAmount resultadoEUR = converter.convertToEntityAttribute(dbDataEUR);

        // Then
        assertThat(resultadoUSD.getCurrency().getCurrencyCode()).isEqualTo("USD");
        assertThat(resultadoEUR.getCurrency().getCurrencyCode()).isEqualTo("EUR");
    }

    @Test
    @DisplayName("Deve retornar null para MonetaryAmount null")
    void deveRetornarNullParaMonetaryAmountNull() {
        // When
        String resultado = converter.convertToDatabaseColumn(null);

        // Then
        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("Deve retornar null para String null")
    void deveRetornarNullParaStringNull() {
        // When
        MonetaryAmount resultado = converter.convertToEntityAttribute(null);

        // Then
        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("Deve retornar null para String vazia")
    void deveRetornarNullParaStringVazia() {
        // When
        MonetaryAmount resultado = converter.convertToEntityAttribute("");

        // Then
        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("Deve lançar exceção para formato inválido")
    void deveLancarExcecaoParaFormatoInvalido() {
        // Given
        String dbData = "100.50"; // Sem moeda

        // When & Then
        assertThatThrownBy(() -> converter.convertToEntityAttribute(dbData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid monetary amount format");
    }

    @Test
    @DisplayName("Deve lançar exceção para formato com múltiplos espaços")
    void deveLancarExcecaoParaFormatoComMultiplosEspacos() {
        // Given
        String dbData = "100.50 BRL USD"; // Múltiplas moedas

        // When & Then
        assertThatThrownBy(() -> converter.convertToEntityAttribute(dbData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid monetary amount format");
    }

    @Test
    @DisplayName("Deve lançar exceção para moeda inválida")
    void deveLancarExcecaoParaMoedaInvalida() {
        // Given
        String dbData = "100.50 INVALID";

        // When & Then
        assertThatThrownBy(() -> converter.convertToEntityAttribute(dbData))
            .isInstanceOf(Exception.class); // Pode ser IllegalArgumentException ou MonetaryException
    }

    @Test
    @DisplayName("Deve converter valor com muitas casas decimais")
    void deveConverterValorComMuitasCasasDecimais() {
        // Given
        MonetaryAmount amount = Money.of(new BigDecimal("100.123456789"), "BRL");

        // When
        String resultado = converter.convertToDatabaseColumn(amount);

        // Then
        assertThat(resultado).isEqualTo("100.123456789 BRL");
    }

    @Test
    @DisplayName("Deve converter valor muito grande")
    void deveConverterValorMuitoGrande() {
        // Given
        MonetaryAmount amount = Money.of(new BigDecimal("999999999.99"), "BRL");

        // When
        String resultado = converter.convertToDatabaseColumn(amount);

        // Then
        assertThat(resultado).isEqualTo("999999999.99 BRL");
    }

    @Test
    @DisplayName("Deve converter valor muito pequeno")
    void deveConverterValorMuitoPequeno() {
        // Given
        MonetaryAmount amount = Money.of(new BigDecimal("0.01"), "BRL");

        // When
        String resultado = converter.convertToDatabaseColumn(amount);

        // Then
        assertThat(resultado).isEqualTo("0.01 BRL");
    }

    @Test
    @DisplayName("Deve manter precisão decimal")
    void deveManterPrecisaoDecimal() {
        // Given
        MonetaryAmount amount = Money.of(new BigDecimal("100.50"), "BRL");

        // When
        String resultado = converter.convertToDatabaseColumn(amount);
        MonetaryAmount convertido = converter.convertToEntityAttribute(resultado);

        // Then
        assertThat(convertido.getNumber().numberValue(BigDecimal.class)).isEqualTo(new BigDecimal("100.5"));
    }

    @Test
    @DisplayName("Deve converter ida e volta corretamente")
    void deveConverterIdaEVoltaCorretamente() {
        // Given
        MonetaryAmount original = Money.of(new BigDecimal("123.45"), "USD");

        // When
        String convertido = converter.convertToDatabaseColumn(original);
        MonetaryAmount reconvertido = converter.convertToEntityAttribute(convertido);

        // Then
        assertThat(reconvertido.getNumber().numberValue(BigDecimal.class))
            .isEqualTo(original.getNumber().numberValue(BigDecimal.class));
        assertThat(reconvertido.getCurrency().getCurrencyCode())
            .isEqualTo(original.getCurrency().getCurrencyCode());
    }
}