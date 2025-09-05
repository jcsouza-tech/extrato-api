package br.com.financas.extrato_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.money.MonetaryAmount;
import org.javamoney.moneta.Money;

@Entity
@Table(name = "transacoes",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"data", "numero_documento", "valor", "banco"},
                name = "uk_transacao_unique"
        ))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private String lancamento;

    private String detalhes;

    @Column(name = "numero_documento")
    private String numeroDocumento;

    @Column(name = "valor", nullable = false, precision = 10, scale = 4)
    private BigDecimal valor;

    @Column(name = "moeda", nullable = false, length = 3)
    private String moeda;
    // Campo transient para API JSR 354 (não persiste no banco)
    @Transient
    private MonetaryAmount valorMonetario;

    @Column(name = "tipo_lancamento")
    private String tipoLancamento;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private String banco;

    @ManyToOne
    @JoinColumn(name = "upload_arquivo_id")
    private UploadArquivo uploadArquivo;

    // Método para garantir a conversão ANTES de persistir
    @PrePersist
    @PreUpdate
    private void prePersist() {
        if (valorMonetario != null) {
            this.valor = valorMonetario.getNumber().numberValue(BigDecimal.class);
            this.moeda = valorMonetario.getCurrency().getCurrencyCode();
        }
        // Garante que moeda nunca seja null
        if (this.moeda == null) {
            this.moeda = "BRL"; // Valor padrão
        }
    }

    // Método para reconstruir o MonetaryAmount após carregar
    @PostLoad
    private void postLoad() {
        if (valor != null && moeda != null) {
            this.valorMonetario = Money.of(valor, moeda);
        }
    }
    
}