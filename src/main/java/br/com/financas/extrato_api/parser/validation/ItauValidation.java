package br.com.financas.extrato_api.parser.validation;

import br.com.financas.extrato_api.config.parser.ItauParserConfig;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Component;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("ItauValidation")
public class ItauValidation implements Validation {
    private final ItauParserConfig config;
    private final Pattern transactionPattern;
    
    public ItauValidation(ItauParserConfig config) {
        this.config = config;
        this.transactionPattern = Pattern.compile(config.getPdf().getTransactionRegex());
    }

    @Override
    public boolean isValidLine(String[] campos) {
        // Para PDF, validamos a linha completa como string
        return isValidTransactionLine(campos[0]);
    }

    public boolean isValidTransactionLine(String linha) {
        if (linha == null || linha.trim().isEmpty()) {
            return false;
        }
        
        String linhaTrimmed = linha.trim();
        
        // Exclui transações que contenham "saldo" (não são transações reais)
        if (linhaTrimmed.toLowerCase().contains("saldo")) {
            return false;
        }
        
        // Verifica se corresponde ao padrão de transação configurado
        return transactionPattern.matcher(linhaTrimmed).find();
    }

    /**
     * Extrai os 4 campos da linha do extrato do Itaú usando regex configurada
     * Formato esperado: "23/07/2025 ELCSS-WIZMARTBSB  -50,00" ou "23/07/2025 PIX TRANSF MARYANN  -100,00  2.178,23"
     * @param linha linha do extrato
     * @return array com [data, descricao, valor, saldo] ou null se não conseguir extrair
     */
    public String[] extrairCamposTransacao(String linha) {
        if (linha == null || linha.trim().isEmpty()) {
            return null;
        }
        
        Matcher matcher = transactionPattern.matcher(linha.trim());
        if (matcher.find()) {
            // Se há dois valores (grupo 4 não é null), o valor real é o último (grupo 4)
            // Se há apenas um valor (grupo 4 é null), o valor real é o primeiro (grupo 3)
            String valor = matcher.group(4) != null ? matcher.group(4) : matcher.group(3);
            String saldo = matcher.group(4) != null ? "" : ""; // Saldo sempre vazio por enquanto
            
            return new String[]{
                matcher.group(1), // data
                matcher.group(2).trim(), // descrição
                valor, // valor (último se houver dois, primeiro se houver apenas um)
                saldo  // saldo (sempre vazio)
            };
        }
        
        return null;
    }


    public boolean validateData(String dataStr) {
        if (dataStr == null || dataStr.isEmpty()) {
            return false;
        }

        try {
            LocalDate.parse(dataStr, DateTimeFormatter.ofPattern(config.getPdf().getDateFormat()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateValor(String valorStr) {
        if (valorStr == null || valorStr.isEmpty()) return false;
        return true; // Validação simplificada - apenas verifica se não está vazio
    }

    public MonetaryAmount parseValorMonetario(String valorStr) {
        try {
            String valorLimpo = cleanField(valorStr);
            
            // Remove espaços e normaliza separadores
            valorLimpo = valorLimpo.replaceAll("\\s+", "").replace(",", ".");
            
            // Trata múltiplos pontos (separadores de milhares)
            if (valorLimpo.contains(".") && valorLimpo.lastIndexOf(".") != valorLimpo.indexOf(".")) {
                String[] parts = valorLimpo.split("\\.");
                if (parts.length > 2) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < parts.length - 1; i++) {
                        sb.append(parts[i]);
                    }
                    sb.append(".").append(parts[parts.length - 1]);
                    valorLimpo = sb.toString();
                }
            }
            
            BigDecimal valorDecimal = new BigDecimal(valorLimpo);
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

    public String gerarNumeroDocumento(String descricao) {
        Pattern numberPattern = Pattern.compile("\\d+");
        Matcher matcher = numberPattern.matcher(descricao);
        
        if (matcher.find()) {
            return matcher.group();
        }
        
        return String.valueOf(descricao.hashCode());
    }

    public String determinarTipoLancamento(MonetaryAmount valor, String descricao) {
        if (valor.isPositive()) {
            return "Entrada";
        } else {
            return "Saída";
        }
    }
}
