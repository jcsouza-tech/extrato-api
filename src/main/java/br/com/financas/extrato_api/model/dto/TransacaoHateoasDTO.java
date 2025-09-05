package br.com.financas.extrato_api.model.dto;

import br.com.financas.extrato_api.model.Transacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
public class TransacaoHateoasDTO extends RepresentationModel<TransacaoHateoasDTO> {
    private Long id;
    private LocalDate data;
    private String lancamento;
    private String detalhes;
    private String numeroDocumento;
    private BigDecimal valor;
    private String tipoLancamento;
    private String categoria;
}
