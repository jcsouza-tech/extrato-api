package br.com.financas.extrato_api.parser.validation;

import br.com.financas.extrato_api.config.parser.BancoDoBrasilParserConfig;
import br.com.financas.extrato_api.config.parser.BankParserConfig;
import br.com.financas.extrato_api.util.CsvColumn;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Component;

import javax.money.MonetaryAmount;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Component("BBValidation")
public class BBValidation implements Validation {
    private final BancoDoBrasilParserConfig config;
    private final MonetaryAmountFormat monetaryFormat;
    public BBValidation(BancoDoBrasilParserConfig config){
        this.config = config;
        this.monetaryFormat = MonetaryFormats.getAmountFormat(new Locale(config.getCsv().getIdioma(), config.getCsv().getPais()));
    }

    public boolean isValidLine(String[] campos) {
        return validateQuantity(campos) &&
                validateTipoLancamento(campos) &&
                validateData(campos) &&
                validateValor(campos);
    }

    private boolean validateQuantity(String[] campos) {
        return campos.length >= 6;
    }

    /**
     * Valida se o tipo do lançamento está preenchido
     * @param campos
     * @return
     */
    private boolean validateTipoLancamento(String[] campos) {
        if(campos[CsvColumn.TIPO_LANCAMENTO.getIndex()].isEmpty())
            return false;

        return !cleanField(campos[CsvColumn.TIPO_LANCAMENTO.getIndex()]).isEmpty();
    }

    private boolean validateData(String[] campos) {
        if (campos.length <= CsvColumn.DATA.getIndex()) return false;

        String dataStr = cleanField(campos[CsvColumn.DATA.getIndex()]);

        // Validações básicas
        if (dataStr.isEmpty() || dataStr.equals("00/00/0000")) {
            return false;
        }

        // Validação com regex
        if (config.getCsv().getDateRegex() != null &&
                !dataStr.matches(config.getCsv().getDateRegex())) {
            return false;
        }

        // Validação adicional do formato de data
        try {
            LocalDate.parse(dataStr, DateTimeFormatter.ofPattern(config.getCsv().getDateFormat()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validateValor(String[] campos) {
        if (campos.length <= CsvColumn.VALOR.getIndex()) return false;

        String valorStr = normalizeDecimal( cleanField( campos[CsvColumn.VALOR.getIndex()] ) );
        String regex = config.getCsv().getValueRegex();
        return valorStr.matches(regex);

    }

    public MonetaryAmount parseValorMonetario(String valorStr) {
        try {
            String valorLimpo = cleanField(valorStr).replace(".", "").replace(",", ".");
            BigDecimal valorDecimal = new BigDecimal(valorLimpo);
            // Parse diretamente do formato brasileiro
            return Money.of(valorDecimal, "BRL");
        } catch (Exception e) {
            throw new IllegalArgumentException("Valor monetário inválido: " + valorStr, e);
        }
    }
    public Optional<MonetaryAmount> parseValorMonetarioSafe(String valorStr) {
        try {
            return Optional.of(parseValorMonetario(valorStr));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public String cleanField(String campo) {
        return campo.replace("\"", "").trim();
    }

    public String normalizeDecimal(String campo) {
        campo = campo.replace(".", "");
        return cleanField(campo).replace(",", ".");
    }
}
