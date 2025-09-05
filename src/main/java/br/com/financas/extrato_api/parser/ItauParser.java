package br.com.financas.extrato_api.parser;

import br.com.financas.extrato_api.model.Transacao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Parser otimizado para extratos PDF do Itaú usando configuração e streams paralelos.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ItauParser implements ExtratoParser {

    @Override
    public Optional<Transacao> parseLine(String linha) {
        return Optional.empty();
    }

    @Override
    public List<Transacao> parse(MultipartFile file) {
        return null;
    }

    @Override
    public String getBankName() {
        return "Itau";
    }

    @Override
    public boolean supports(String fileName) {
        return false;
    }
}
