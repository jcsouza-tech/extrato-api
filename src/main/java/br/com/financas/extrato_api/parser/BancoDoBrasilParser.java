package br.com.financas.extrato_api.parser;

import br.com.financas.extrato_api.config.parser.BancoDoBrasilParserConfig;
import br.com.financas.extrato_api.config.parser.BankParserConfig;
import br.com.financas.extrato_api.model.Transacao;
import br.com.financas.extrato_api.parser.validation.Validation;
import br.com.financas.extrato_api.util.CsvColumn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.money.MonetaryAmount;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Parser otimizado para Banco do Brasil usando configuração e streams paralelos.
 */
@Slf4j
@Component("BBparser")
@RequiredArgsConstructor
public class BancoDoBrasilParser implements ExtratoParser {

    private final BancoDoBrasilParserConfig config;
    private final DateTimeFormatter dateFormatter;
    private final Validation validation;
    @Autowired
    public BancoDoBrasilParser(BancoDoBrasilParserConfig config, @Qualifier("BBValidation") Validation validation) {
        this.config = config;
        this.dateFormatter = DateTimeFormatter.ofPattern(config.getCsv().getDateFormat());
        this.validation = validation;
    }

    /**
     * Transforma uma linha do extrato em uma Transcao
     * @param linha linha do extrato
     * @return return Transacao
     */
    @Override
    public Optional<Transacao> parseLine(String linha) {
        String[] campos = linha.split(config.getCsv().getSeparator(), -1);
        if (!validation.isValidLine(campos)) {
            return Optional.empty();
        }

        try {
            LocalDate data = LocalDate.parse(validation.cleanField(campos[ CsvColumn.DATA.getIndex() ]), dateFormatter);
            // Parse como MonetaryAmount
            MonetaryAmount valorMonetario = validation.parseValorMonetario(campos[CsvColumn.VALOR.getIndex()]);
            return Optional.of(Transacao.builder()
                    .id(null)
                    .data(data)
                    .lancamento(validation.cleanField( campos[CsvColumn.LANCAMENTO.getIndex()] ))
                    .detalhes(validation.cleanField( campos[CsvColumn.DETALHES.getIndex()] ) )
                    .numeroDocumento(validation.cleanField( campos[CsvColumn.NUMERO_DOCUMENTO.getIndex()] ))
                    .valorMonetario(valorMonetario)
                    .tipoLancamento(validation.cleanField( campos[CsvColumn.TIPO_LANCAMENTO.getIndex()] ) )
                    .categoria("PENDENTE")
                    .banco(config.getName())
                    .build());

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Recebe um arquivo de extrato e trata o conteúdo de forma a devolver uma lista de transacoes
     * e caso o arquivo não seja suportado devolve uma lista vazia.
     * @param file Arquivo de extrato em formato CSV
     * @throws RuntimeException se o arquivo tiver erro de IO, lança runtime com a mensagem do erro de processamento
     * @return List com todas as transações validas do arquivo
     */
    @Override
    public List<Transacao> parse(MultipartFile file) throws RuntimeException{
        if( !supports(file.getOriginalFilename()))
            return List.of();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.ISO_8859_1))) {
            return reader.lines()
                    .skip(config.getCsv().getSkipLine())
                    .parallel()
                    .map(this::parseLine)
                    .flatMap(Optional::stream)
                    .toList();
        }catch (IOException ioException){
            throw new RuntimeException("Erro ao processar o arquivo: " + ioException.getMessage(), ioException);
        }
    }

    @Override
    public BankParserConfig getConfig() {
        return config;
    }

    /** Retorna o nome do banco
     * @return nome do banco que é suportado por este parser
     */
    @Override
    public String getBankName() {
        return config.getName();
    }
}