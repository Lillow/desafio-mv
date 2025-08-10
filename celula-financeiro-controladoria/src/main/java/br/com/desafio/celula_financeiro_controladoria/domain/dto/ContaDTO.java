package br.com.desafio.celula_financeiro_controladoria.domain.dto;

import java.math.BigDecimal;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.base.BaseEntityDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.StatusConta;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoConta;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO de Conta para tráfego entre camadas/integração.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ContaDTO extends BaseEntityDTO {

    private String documento;
    private TipoConta tipoConta;
    private String agencia;
    private String numero;
    private StatusConta status;
    private BigDecimal saldo = BigDecimal.ZERO;
    private ClienteDTO cliente;
}
