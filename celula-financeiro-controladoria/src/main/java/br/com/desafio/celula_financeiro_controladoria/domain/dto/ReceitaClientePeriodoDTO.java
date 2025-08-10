package br.com.desafio.celula_financeiro_controladoria.domain.dto;

import java.math.BigDecimal;

public record ReceitaClientePeriodoDTO(
        Long clienteId,
        String cliente,
        long quantidadeMovs,
        BigDecimal valorCobrado) {
}
