package br.com.financas.extrato_api.parser;

import br.com.financas.extrato_api.config.parser.BankParserConfig;
import br.com.financas.extrato_api.config.parser.ItauParserConfig;
import br.com.financas.extrato_api.model.Transacao;
import br.com.financas.extrato_api.parser.validation.ItauValidation;
import br.com.financas.extrato_api.parser.validation.Validation;
import br.com.financas.extrato_api.util.ItauColumn;
import br.com.financas.extrato_api.util.PdfTextStripper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.money.MonetaryAmount;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parser para extratos PDF do Itaú seguindo o padrão de configuração
 */
@Slf4j
@Component("ItauParser")
@RequiredArgsConstructor
public class ItauParser implements ExtratoParser {

    private final ItauParserConfig config;
    private final DateTimeFormatter dateFormatter;
    private final Validation validation;

    @Autowired
    public ItauParser(ItauParserConfig config, @Qualifier("ItauValidation") Validation validation) {
        this.config = config;
        this.dateFormatter = DateTimeFormatter.ofPattern(config.getPdf().getDateFormat());
        this.validation = validation;
    }

    /**
     * Transforma uma linha do extrato em uma Transacao
     * @param linha linha do extrato
     * @return Transacao
     */
    @Override
    public Optional<Transacao> parseLine(String linha) {
        if (linha == null || linha.trim().isEmpty()) {
            return Optional.empty();
        }
        
        linha = linha.trim();
        
        // Usa a validação para verificar se é uma linha válida
        if (!((ItauValidation) validation).isValidTransactionLine(linha)) {
            return Optional.empty();
        }

        try {
            // Extrai os 4 campos usando o novo método
            String[] campos = ((ItauValidation) validation).extrairCamposTransacao(linha);
            
            if (campos == null) {
                log.debug("Não foi possível extrair campos da linha: {}", linha);
                return Optional.empty();
            }
            
            // Usa o enum para acessar os campos de forma mais clara
            String dataStr = campos[ItauColumn.DATA.getIndex()];
            String descricao = campos[ItauColumn.DESCRICAO.getIndex()];
            String valorStr = campos[ItauColumn.VALOR.getIndex()];
            String saldoStr = campos[ItauColumn.SALDO.getIndex()];
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(config.getPdf().getDateFormat());
            LocalDate data = LocalDate.parse(dataStr, dateFormatter);
            MonetaryAmount valorMonetario = validation.parseValorMonetario(valorStr);
            
            // A descrição já vem completa do regex (sem data adicional)
            String descricaoCompleta = descricao;
            
            return Optional.of(Transacao.builder()
                    .id(null)
                    .data(data)
                    .lancamento(descricaoCompleta)
                    .detalhes(descricaoCompleta)
                    .numeroDocumento(((ItauValidation) validation).gerarNumeroDocumento(descricaoCompleta))
                    .valor(valorMonetario.getNumber().numberValueExact(BigDecimal.class))
                    .moeda(valorMonetario.getCurrency().getCurrencyCode())
                    .valorMonetario(valorMonetario)
                    .tipoLancamento(((ItauValidation) validation).determinarTipoLancamento(valorMonetario, descricaoCompleta))
                    .categoria("PENDENTE")
                    .banco(config.getName())
                    .build());

        } catch (Exception e) {
            log.warn("Erro ao parsear linha: {}", linha, e);
            return Optional.empty();
        }
    }

    /**
     * Recebe um arquivo de extrato PDF e trata o conteúdo de forma a devolver uma lista de transacoes
     * @param file Arquivo de extrato em formato PDF
     * @throws RuntimeException se o arquivo tiver erro de IO
     * @return List com todas as transações validas do arquivo
     */
    @Override
    public List<Transacao> parse(MultipartFile file) throws RuntimeException {
        if (!supports(file.getOriginalFilename())) {
            return List.of();
        }
        
        try {
            Path tempFile = Files.createTempFile("itau_extrato_", ".pdf");
            file.transferTo(tempFile.toFile());
            
            try {
                String text = PdfTextStripper.extractText(tempFile.toFile());
                log.info("Texto extraído do PDF: {} caracteres", text.length());
                
                List<Transacao> transacoes = parseText(text);
                log.info("{} transações parseadas do arquivo {}", transacoes.size(), file.getOriginalFilename());
                
                return transacoes;
                
            } finally {
                Files.deleteIfExists(tempFile);
            }
            
        } catch (IOException e) {
            log.error("Erro ao processar arquivo PDF do Itaú: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Erro ao processar arquivo PDF: " + e.getMessage(), e);
        }
    }
    
    private List<Transacao> parseText(String text) {
        List<Transacao> transacoes = new ArrayList<>();
        String[] lines = text.split("\\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            Optional<Transacao> transacao = parseLine(line);
            transacao.ifPresent(transacoes::add);
        }
        
        return transacoes;
    }

    @Override
    public BankParserConfig getConfig() {
        return config;
    }

    /**
     * Retorna o nome do banco
     * @return nome do banco que é suportado por este parser
     */
    @Override
    public String getBankName() {
        return config.getName();
    }
}
