package br.com.desafio.celula_financeiro_controladoria.domain.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.base.BaseEntityDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoMovimentacao;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Payload recebido da Instituição Financeira.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MovimentacaoDTO extends BaseEntityDTO {
    private String instituicao;
    private String documentoCliente;
    private String agencia;
    private String numeroConta;
    private TipoMovimentacao tipo;
    private BigDecimal valor;
    private String descricao;
    private OffsetDateTime dataEvento;
}
