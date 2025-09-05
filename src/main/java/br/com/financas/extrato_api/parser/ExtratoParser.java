package br.com.financas.extrato_api.parser;

import br.com.financas.extrato_api.model.Transacao;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

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
     * Verifica se este parser suporta o arquivo pelo nome.
     */
    boolean supports(String fileName);
}
