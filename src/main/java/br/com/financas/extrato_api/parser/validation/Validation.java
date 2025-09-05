package br.com.financas.extrato_api.parser.validation;

import javax.money.MonetaryAmount;
import java.util.Optional;

public interface Validation {
    boolean isValidLine(String[] campos);

    String cleanField(String campo);
    String normalizeDecimal(String campo);
    MonetaryAmount parseValorMonetario(String valorStr);
    Optional<MonetaryAmount> parseValorMonetarioSafe(String valorStr);
}
