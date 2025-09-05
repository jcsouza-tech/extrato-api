package br.com.financas.extrato_api.model.parser;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class PdfConfig {
    private String dateFormat;
    private String dateRegex;
    private String valueRegex;
    private TextExtractionConfig textExtraction;
    
    @Getter
    @Setter
    public static class TextExtractionConfig {
        private boolean sortByPosition;
        private String encoding;
    }
}