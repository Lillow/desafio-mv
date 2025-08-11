package br.com.desafio.celula_financeiro_controladoria.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.base.BaseEntityDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RelatorioSaldoDTO extends BaseEntityDTO {
    private Long clienteId;
    private String clienteNome;
    private LocalDate clienteDesde;
    private EnderecoDTO endereco;

    private long qtdCredito;
    private long qtdDebito;
    private long totalMovs;
    private BigDecimal valorPagoMovimentacoes; // tarifa
    private BigDecimal saldoInicial;
    private BigDecimal saldoAtual;
    private LocalDate dataRefInicio;
    private LocalDate dataRefFim;
}
