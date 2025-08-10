package br.com.desafio.celula_financeiro_controladoria.domain.dto.base;

import lombok.Data;

@Data
public abstract class BaseEntityDTO {
    private Long id;
    private Boolean ativo = true;

}
