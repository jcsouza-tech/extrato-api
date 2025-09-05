package br.com.financas.extrato_api.model.dto;

import br.com.financas.extrato_api.controller.ExtratoController;
import br.com.financas.extrato_api.model.Transacao;
import org.jetbrains.annotations.NotNull;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Classe para tratar a conversão de Transacao para TransacaoHateoasDTO implementando o HATEOAS
 */
@Component
public class TransacaoRepresentationAssembler implements RepresentationModelAssembler<Transacao, TransacaoHateoasDTO> {
    
    /**
     * Converte uma entidade Transacao e retorna uma TransacaoHateoasDTO com links HATEOAS
     * @param entity entidade Transacao
     * @return bean TransacaoHateoasDTO com links
     */
    @NotNull
    @Override
    public TransacaoHateoasDTO toModel(Transacao entity) {
        TransacaoHateoasDTO dto = TransacaoHateoasDTO.builder()
                .id(entity.getId())
                .data(entity.getData())
                .lancamento(entity.getLancamento())
                .detalhes(entity.getDetalhes())
                .numeroDocumento(entity.getNumeroDocumento())
                .valor(entity.getValor())
                .tipoLancamento(entity.getTipoLancamento())
                .categoria(entity.getCategoria())
                .build();
        
        // Adiciona links HATEOAS
        dto.add(linkTo(methodOn(ExtratoController.class).visualizarExtrato()).withRel("extrato"));
        dto.add(linkTo(methodOn(ExtratoController.class).carregarExtrato(null,null)).withRel("carregar"));
        
        return dto;
    }

    /**
     * Conversão de uma coleção de Transacoes para CollectionModel com HATEOAS
     * @param entities coleção de entidades
     * @return CollectionModel<TransacaoHateoasDTO>
     */
    @NotNull
    @Override
    public CollectionModel<TransacaoHateoasDTO> toCollectionModel(@NotNull Iterable<? extends Transacao> entities) {
        CollectionModel<TransacaoHateoasDTO> collectionModel = RepresentationModelAssembler.super.toCollectionModel(entities);
        
        // Adiciona links para a coleção
        collectionModel.add(linkTo(methodOn(ExtratoController.class).visualizarExtrato()).withSelfRel());
        collectionModel.add(linkTo(methodOn(ExtratoController.class).carregarExtrato(null, null)).withRel("carregar"));
        
        return collectionModel;
    }
}
