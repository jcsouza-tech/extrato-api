package br.com.financas.extrato_api.service;

import br.com.financas.extrato_api.model.Transacao;
import br.com.financas.extrato_api.model.UploadArquivo;
import br.com.financas.extrato_api.model.dto.ProcessamentoResult;
import br.com.financas.extrato_api.parser.ItauParser;
import br.com.financas.extrato_api.repository.TransacaoRepository;
import br.com.financas.extrato_api.repository.UploadArquivoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service para processamento de extratos PDF do Itaú
 */
@Slf4j
@Service("itau-service")
@RequiredArgsConstructor
public class ItauService implements ExtratoService {

    private final ItauParser itauParser;
    private final TransacaoRepository transacaoRepository;
    private final UploadArquivoRepository uploadArquivoRepository;

    @Override
    @Transactional
    public ProcessamentoResult processarArquivo(MultipartFile file) {
        log.info("Iniciando processamento do arquivo PDF do Itaú: {}, tamanho: {} bytes", 
                file.getOriginalFilename(), file.getSize());

        try {
            // Calcular hash do arquivo para verificar duplicatas
            String hashArquivo = calcularHashArquivo(file.getBytes());
            log.debug("Hash calculado para arquivo {}: {}", file.getOriginalFilename(), hashArquivo);

            // Verificar se arquivo já foi processado
            if (uploadArquivoRepository.existsByHashArquivo(hashArquivo)) {
                log.warn("Arquivo duplicado detectado: {} (hash: {})", file.getOriginalFilename(), hashArquivo);
                return ProcessamentoResult.arquivoDuplicado(file.getOriginalFilename());
            }

            // Parsear transações do PDF
            List<Transacao> transacoes = itauParser.parse(file);
            log.info("{} transações parseadas do arquivo {}", transacoes.size(), file.getOriginalFilename());

            if (transacoes.isEmpty()) {
                log.warn("Nenhuma transação encontrada no arquivo: {}", file.getOriginalFilename());
                return ProcessamentoResult.sucesso(file.getOriginalFilename(), 0);
            }

            // Salvar upload do arquivo
            UploadArquivo upload = salvarUploadArquivo(file, hashArquivo);
            log.debug("Upload salvo com ID: {}", upload.getId());

            // Associar transações ao upload
            transacoes.forEach(transacao -> transacao.setUploadArquivo(upload));

            // Salvar transações
            int transacoesSalvas = salvarTransacoes(transacoes);
            log.info("{} transações salvas em lote com sucesso", transacoesSalvas);

            log.info("Processamento concluído com sucesso: {} transações salvas para arquivo {}", 
                    transacoesSalvas, file.getOriginalFilename());

            return ProcessamentoResult.sucesso(file.getOriginalFilename(), transacoesSalvas);

        } catch (Exception e) {
            log.error("Erro ao processar arquivo PDF do Itaú: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Erro ao processar arquivo PDF: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transacao> getExtrato() {
        log.debug("Buscando extrato do Itaú");
        return transacaoRepository.findAll().stream()
                .filter(t -> "Itaú".equals(t.getBanco()))
                .sorted((t1, t2) -> t2.getData().compareTo(t1.getData()))
                .toList();
    }

    /**
     * Calcula hash MD5 do conteúdo do arquivo
     */
    private String calcularHashArquivo(byte[] conteudo) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(conteudo);
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (NoSuchAlgorithmException e) {
            log.error("Erro ao calcular hash MD5", e);
            throw new RuntimeException("Erro ao calcular hash do arquivo", e);
        }
    }

    /**
     * Salva informações do upload do arquivo
     */
    private UploadArquivo salvarUploadArquivo(MultipartFile file, String hashArquivo) {
        UploadArquivo upload = new UploadArquivo();
        upload.setNomeArquivo(file.getOriginalFilename());
        upload.setHashArquivo(hashArquivo);
        upload.setBanco("Itaú");
        upload.setDataUpload(LocalDate.now());

        return uploadArquivoRepository.save(upload);
    }

    /**
     * Salva transações verificando duplicatas antes de salvar
     */
    private int salvarTransacoes(List<Transacao> transacoes) {
        int salvas = 0;
        
        for (Transacao transacao : transacoes) {
            try {
                // Verificar se transação já existe antes de salvar
                boolean exists = transacaoRepository.existsByDataAndNumeroDocumentoAndValorAndBanco(
                        transacao.getData(), 
                        transacao.getNumeroDocumento(), 
                        transacao.getValor(), 
                        transacao.getBanco()
                );
                
                if (!exists) {
                    transacaoRepository.save(transacao);
                    salvas++;
                    log.debug("Transação salva: {} | {} | {}", 
                            transacao.getData(), transacao.getLancamento(), transacao.getValor());
                } else {
                    log.debug("Transação duplicada ignorada: {} | {} | {} | {}",
                            transacao.getData(), transacao.getLancamento(), 
                            transacao.getValor(), transacao.getBanco());
                }
                
            } catch (Exception e) {
                log.warn("Erro ao salvar transação individual: {} | {} | {}", 
                        transacao.getData(), transacao.getLancamento(), transacao.getValor(), e);
            }
        }
        
        return salvas;
    }

    public String getBankName() {
        return "Itaú";
    }

    public boolean supports(String fileName) {
        return itauParser.supports(fileName);
    }
}
