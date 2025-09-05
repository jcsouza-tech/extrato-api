package br.com.financas.extrato_api.service;

import br.com.financas.extrato_api.exception.ArquivoProcessamentoException;
import br.com.financas.extrato_api.exception.FormatoArquivoInvalidoException;
import br.com.financas.extrato_api.exception.HashCalculationException;
import br.com.financas.extrato_api.model.Transacao;
import br.com.financas.extrato_api.model.UploadArquivo;
import br.com.financas.extrato_api.model.dto.ProcessamentoResult;
import br.com.financas.extrato_api.parser.ExtratoParser;
import br.com.financas.extrato_api.repository.TransacaoRepository;
import br.com.financas.extrato_api.repository.UploadArquivoRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import br.com.financas.extrato_api.observability.ExtratoMetricsService;

@Slf4j
@Service("banco-do-brasil-service")
public class BancoDoBrasilService implements ExtratoService {
    private final TransacaoRepository transacaoRepository;
    private final UploadArquivoRepository uploadArquivoRepository;
    private final ExtratoParser bbParser;
    private final ExtratoMetricsService metricsService;
    private final EntityManager entityManager;

    @Autowired
    public BancoDoBrasilService(TransacaoRepository transacaoRepository, UploadArquivoRepository uploadArquivoRepository,@Qualifier("BBparser") ExtratoParser extratoParser, ExtratoMetricsService metricsService, EntityManager entityManager) {
        this.transacaoRepository = transacaoRepository;
        this.uploadArquivoRepository = uploadArquivoRepository;
        this.bbParser = extratoParser;
        this.metricsService = metricsService;
        this.entityManager = entityManager;
    }

    /**
     * Processa arquivo do banco.
     */
    public ProcessamentoResult processarArquivo(MultipartFile file) {
        long startTime = System.currentTimeMillis();
        log.info("Iniciando processamento do arquivo: {}, tamanho: {} bytes",
                file.getOriginalFilename(), file.getSize());

        // Validações básicas
        if (file.isEmpty()) {
            throw new ArquivoProcessamentoException("Arquivo está vazio");
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new FormatoArquivoInvalidoException("Arquivo deve ser CSV");
        }
        String fileHash = "";
        try{
            // Calcular hash do arquivo
            fileHash = calcularHash(file);
            log.debug("Hash calculado para arquivo {}: {}", file.getOriginalFilename(), fileHash);
        }catch(IOException ioException){
            return ProcessamentoResult.erroProcessamento( file.getOriginalFilename(), ioException.getMessage() );
        }

        // Verificar se arquivo já foi processado
        if (uploadArquivoRepository.existsByHashArquivo(fileHash)) {
            log.warn("Arquivo duplicado detectado: {} (hash: {})",
                    file.getOriginalFilename(), fileHash);
            return ProcessamentoResult.arquivoDuplicado(file.getOriginalFilename());
        }

        // Processar transações
        List<Transacao> transacoes = bbParser.parse(file);
        log.info("{} transações parseadas do arquivo {}",
                transacoes.size(), file.getOriginalFilename());

        // Salvar upload e transações
        UploadArquivo upload = salvarUploadArquivo(file, fileHash, bbParser.getBankName());
        List<Transacao> transacoesSalvas = salvarTransacoes(transacoes, upload);

        log.info("Processamento concluído com sucesso: {} transações salvas para arquivo {}",
                transacoesSalvas.size(), file.getOriginalFilename());

        // Métricas de sucesso
        metricsService.incrementarArquivosProcessados();
        metricsService.incrementarTransacoesProcessadas(transacoesSalvas.size());
        metricsService.incrementarBancoUtilizado("banco-do-brasil");
        // Métricas de tempo
        long duration = System.currentTimeMillis() - startTime;
        metricsService.registrarTempoProcessamento(Duration.ofMillis(duration));

        return ProcessamentoResult.sucesso(file.getOriginalFilename(), transacoesSalvas.size());

    }

    /**
     * Retorna todas as transações.
     */
    public List<Transacao> getExtrato() {
        return transacaoRepository.findAll();
    }

    /**
     * Retorna transações por período.
     */
    public List<Transacao> getExtratoPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return transacaoRepository.findByDataBetween(dataInicio, dataFim);
    }
    private String calcularHash(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            int totalRead = 0;

            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
                totalRead += read;
            }

            log.debug("Total de bytes lidos para hash: {}", totalRead);
            byte[] hash = digest.digest();
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Algoritmo SHA-256 não disponível no ambiente", e);
            throw new HashCalculationException("Algoritmo de hash não disponível", e);
        }
    }

    private UploadArquivo salvarUploadArquivo(MultipartFile file, String hash, String banco) {
        UploadArquivo upload = new UploadArquivo();
        upload.setHashArquivo(hash);
        upload.setNomeArquivo(file.getOriginalFilename());
        upload.setDataUpload(LocalDate.now());
        upload.setBanco(banco);

        UploadArquivo savedUpload = uploadArquivoRepository.save(upload);
        log.debug("Upload salvo com ID: {}", savedUpload.getId());
        return savedUpload;
    }

    private List<Transacao> salvarTransacoes(List<Transacao> transacoes, UploadArquivo upload) {
        // Prepara todas as transações com o upload
        transacoes.forEach(transacao -> transacao.setUploadArquivo(upload));

        try {
            // Tenta salvar em lote primeiro (mais eficiente)
            List<Transacao> transacoesSalvas = transacaoRepository.saveAll(transacoes);
            log.info("{} transações salvas em lote com sucesso", transacoesSalvas.size());
            return transacoesSalvas;

        } catch (DataIntegrityViolationException e) {
            log.warn("Violação de integridade detectada, salvando transações individualmente");
            // Limpa a sessão para evitar problemas com entidades com ID nulo
            try {
                entityManager.flush();
            } catch (Exception ignored) {
                // Ignora erros de flush
            }
            entityManager.clear();
            
            // Salva transações individualmente diretamente aqui
            List<Transacao> transacoesSalvas = new ArrayList<>();
            int duplicatas = 0;

            for (Transacao transacao : transacoes) {
                try {
                    transacao.setUploadArquivo(upload);
                    Transacao salva = transacaoRepository.save(transacao);
                    transacoesSalvas.add(salva);
                } catch (DataIntegrityViolationException duplicata) {
                    // Transação duplicada (unique constraint violada)
                    duplicatas++;
                    log.debug("Transação duplicada ignorada: {}", transacao);
                }
            }

            if (duplicatas > 0) {
                log.info("{} transações salvas, {} duplicatas ignoradas",
                        transacoesSalvas.size(), duplicatas);
            }

            return transacoesSalvas;
        }
    }
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

}


