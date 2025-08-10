package br.com.desafio.celula_financeiro_controladoria.domain.dto;

import java.math.BigDecimal;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.base.BaseEntityDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReceitaClienteDTO extends BaseEntityDTO {
    private Long clienteId;
    private String clienteNome;
    private long quantidadeMovs;
    private BigDecimal valorTarifado;
}
