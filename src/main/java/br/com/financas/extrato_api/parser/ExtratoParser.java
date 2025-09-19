package br.com.financas.extrato_api.parser;

import br.com.financas.extrato_api.config.parser.BankParserConfig;
import br.com.financas.extrato_api.model.Transacao;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Interface simples para parser de extratos bancários.
 */
public interface ExtratoParser {
    Optional<Transacao> parseLine(String linha);
    
    /**
     * Processa um arquivo de extrato e retorna uma lista de transações.
     */
    List<Transacao> parse(MultipartFile file);
    
    /**
     * Retorna o nome do banco suportado.
     */
    String getBankName();
    
    /**
     * Retorna a configuração do parser.
     */
    BankParserConfig getConfig();
    
    /**
     * Verifica se este parser suporta o arquivo pelo nome.
     * Implementação padrão que pode ser sobrescrita se necessário.
     */
    default boolean supports(String fileName) {
        if (fileName == null) return false;

        // Verifica extensão
        String extension = fileName.contains(".") ?
                fileName.substring(fileName.lastIndexOf(".")) : "";
        if (!getConfig().getSupportedExtensions().contains(extension.toLowerCase())) {
            return false;
        }

        // Verifica padrões de nome de arquivo
        String lowerFileName = fileName.toLowerCase();
        List<Pattern> filePatterns = getConfig().getFilePatterns().stream()
                .map(Pattern::compile)
                .toList();
        
        return filePatterns.stream()
                .anyMatch(pattern -> pattern.matcher(lowerFileName).matches());
    }
}
