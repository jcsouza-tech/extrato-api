package br.com.financas.extrato_api.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitário para extrair texto de PDFs de extratos bancários
 * Especialmente desenvolvido para análise de extratos do Itaú
 */
public class PdfTextStripper {
    
    private static final Logger log = LoggerFactory.getLogger(PdfTextStripper.class);
    
    /**
     * Extrai todo o texto de um PDF
     * @param pdfFile Arquivo PDF
     * @return Texto extraído do PDF
     * @throws IOException Se houver erro na leitura do arquivo
     */
    public static String extractText(File pdfFile) throws IOException {
        log.info("Iniciando extração de texto do PDF: {}", pdfFile.getName());
        
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            log.info("Texto extraído com sucesso. Tamanho: {} caracteres", text.length());
            return text;
        }
    }
    
    /**
     * Extrai texto de uma página específica do PDF
     * @param pdfFile Arquivo PDF
     * @param pageNumber Número da página (1-based)
     * @return Texto da página específica
     * @throws IOException Se houver erro na leitura do arquivo
     */
    public static String extractTextFromPage(File pdfFile, int pageNumber) throws IOException {
        log.info("Extraindo texto da página {} do PDF: {}", pageNumber, pdfFile.getName());
        
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(pageNumber);
            stripper.setEndPage(pageNumber);
            String text = stripper.getText(document);
            
            log.info("Texto da página {} extraído com sucesso. Tamanho: {} caracteres", pageNumber, text.length());
            return text;
        }
    }
    
    /**
     * Extrai texto de um range de páginas do PDF
     * @param pdfFile Arquivo PDF
     * @param startPage Página inicial (1-based)
     * @param endPage Página final (1-based)
     * @return Texto das páginas especificadas
     * @throws IOException Se houver erro na leitura do arquivo
     */
    public static String extractTextFromPages(File pdfFile, int startPage, int endPage) throws IOException {
        log.info("Extraindo texto das páginas {} a {} do PDF: {}", startPage, endPage, pdfFile.getName());
        
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(startPage);
            stripper.setEndPage(endPage);
            String text = stripper.getText(document);
            
            log.info("Texto das páginas {} a {} extraído com sucesso. Tamanho: {} caracteres", startPage, endPage, text.length());
            return text;
        }
    }
    
    /**
     * Extrai texto de todas as páginas e retorna como lista
     * @param pdfFile Arquivo PDF
     * @return Lista com o texto de cada página
     * @throws IOException Se houver erro na leitura do arquivo
     */
    public static List<String> extractTextByPages(File pdfFile) throws IOException {
        log.info("Extraindo texto página por página do PDF: {}", pdfFile.getName());
        
        List<String> pages = new ArrayList<>();
        
        try (PDDocument document = PDDocument.load(pdfFile)) {
            int totalPages = document.getNumberOfPages();
            log.info("PDF possui {} páginas", totalPages);
            
            for (int i = 1; i <= totalPages; i++) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String pageText = stripper.getText(document);
                pages.add(pageText);
                
                log.debug("Página {} extraída: {} caracteres", i, pageText.length());
            }
        }
        
        log.info("Extração completa: {} páginas processadas", pages.size());
        return pages;
    }
    
    /**
     * Analisa a estrutura do PDF e identifica padrões
     * @param pdfFile Arquivo PDF
     * @return Informações sobre a estrutura do PDF
     * @throws IOException Se houver erro na leitura do arquivo
     */
    public static PdfStructureInfo analyzeStructure(File pdfFile) throws IOException {
        log.info("Analisando estrutura do PDF: {}", pdfFile.getName());
        
        try (PDDocument document = PDDocument.load(pdfFile)) {
            int totalPages = document.getNumberOfPages();
            log.info("PDF possui {} páginas", totalPages);
            
            // Extrair primeira página para análise
            String firstPageText = extractTextFromPage(pdfFile, 1);
            
            PdfStructureInfo info = new PdfStructureInfo();
            info.setTotalPages(totalPages);
            info.setFirstPageText(firstPageText);
            info.setFileName(pdfFile.getName());
            
            // Análise básica de padrões
            analyzePatterns(info, firstPageText);
            
            return info;
        }
    }
    
    /**
     * Analisa padrões no texto extraído
     * @param info Informações da estrutura do PDF
     * @param text Texto para análise
     */
    private static void analyzePatterns(PdfStructureInfo info, String text) {
        log.info("Analisando padrões no texto extraído");
        
        // Procurar por padrões comuns de extratos bancários
        String[] patterns = {
            "ITAU", "Itaú", "ITAÚ",
            "EXTRATO", "Extrato",
            "CONTA", "Conta",
            "SALDO", "Saldo",
            "DATA", "Data",
            "VALOR", "Valor",
            "LANÇAMENTO", "Lançamento",
            "DOCUMENTO", "Documento"
        };
        
        for (String pattern : patterns) {
            if (text.contains(pattern)) {
                info.addFoundPattern(pattern);
                log.debug("Padrão encontrado: {}", pattern);
            }
        }
        
        // Procurar por padrões de data
        if (text.matches(".*\\d{2}/\\d{2}/\\d{4}.*")) {
            info.addFoundPattern("DATA_FORMAT_DD/MM/YYYY");
        }
        
        // Procurar por padrões de valor monetário
        if (text.matches(".*\\d{1,3}(\\.\\d{3})*,\\d{2}.*")) {
            info.addFoundPattern("VALOR_FORMAT_BR");
        }
        
        log.info("Análise de padrões concluída. {} padrões encontrados", info.getFoundPatterns().size());
    }
    
    /**
     * Classe para armazenar informações sobre a estrutura do PDF
     */
    public static class PdfStructureInfo {
        private String fileName;
        private int totalPages;
        private String firstPageText;
        private List<String> foundPatterns = new ArrayList<>();
        
        // Getters e Setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
        
        public String getFirstPageText() { return firstPageText; }
        public void setFirstPageText(String firstPageText) { this.firstPageText = firstPageText; }
        
        public List<String> getFoundPatterns() { return foundPatterns; }
        public void addFoundPattern(String pattern) { this.foundPatterns.add(pattern); }
        
        @Override
        public String toString() {
            return String.format("PdfStructureInfo{fileName='%s', totalPages=%d, patterns=%s}", 
                fileName, totalPages, foundPatterns);
        }
    }
}
