package br.com.financas.extrato_api.repository;

import br.com.financas.extrato_api.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    
    /**
     * Busca transações por período.
     */
    List<Transacao> findByDataBetween(LocalDate dataInicio, LocalDate dataFim);
    
    /**
     * Verifica se já existe uma transação com a mesma data, valor e número do documento.
     * Usado para evitar duplicatas.
     */
    @Deprecated
    boolean existsByDataAndValorAndNumeroDocumento(LocalDate data, double valor, String numeroDocumento);
    /**
     * Verifica se já existe uma transação com a mesma data, valor, número do documento, Banco.
     * Usado para evitar duplicatas.
     */
    boolean existsByDataAndNumeroDocumentoAndValorAndBanco(
            LocalDate data, String numeroDocumento, BigDecimal valor, String banco);
}