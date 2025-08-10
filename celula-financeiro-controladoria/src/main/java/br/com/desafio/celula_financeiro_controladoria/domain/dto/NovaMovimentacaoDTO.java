package br.com.desafio.celula_financeiro_controladoria.domain.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoMovimento;

public record NovaMovimentacaoDTO(
        TipoMovimento tipo,
        BigDecimal valor,
        String descricao,
        String origem,
        OffsetDateTime dataHora) {
}