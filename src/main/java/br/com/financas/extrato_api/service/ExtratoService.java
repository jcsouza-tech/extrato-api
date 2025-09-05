package br.com.financas.extrato_api.service;

import br.com.financas.extrato_api.model.Transacao;
import br.com.financas.extrato_api.model.dto.ProcessamentoResult;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ExtratoService {

    static String cleanAndTrimCsvField(String campo) {
        return campo.replace("\"", "").trim();
    }

    static String replaceCommaWithPeriod(String campo) {
        return campo.replace("\"", "").replace(",", ".").trim();
    }
    @Transactional(
            rollbackFor = Exception.class,        // Rollback para qualquer exceção
            propagation = Propagation.REQUIRED,   // Comportamento de propagação
            isolation = Isolation.READ_COMMITTED, // Nível de isolamento
            timeout = 30,                         // Timeout em segundos
            readOnly = false                      // Modo apenas leitura
    )
    ProcessamentoResult processarArquivo(MultipartFile file);
    @Transactional(readOnly = true)
    List<Transacao> getExtrato();
}
