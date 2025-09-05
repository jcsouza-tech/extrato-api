package br.com.financas.extrato_api.util;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.javamoney.moneta.Money;

import java.math.BigDecimal;

@Converter(autoApply = true)
public class MonetaryAmountConverter implements AttributeConverter<MonetaryAmount, String> {
    
    private static final String SEPARATOR = " ";
    
    @Override
    public String convertToDatabaseColumn(MonetaryAmount monetaryAmount) {
        if (monetaryAmount == null) {
            return null;
        }
        return monetaryAmount.getNumber() + SEPARATOR + monetaryAmount.getCurrency();
    }
    
    @Override
    public MonetaryAmount convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        String[] parts = dbData.split(SEPARATOR);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid monetary amount format: " + dbData);
        }
        BigDecimal amount = new BigDecimal(parts[0]);
        CurrencyUnit currency = Monetary.getCurrency(parts[1]);
        return Money.of(amount, currency);
    }
}
