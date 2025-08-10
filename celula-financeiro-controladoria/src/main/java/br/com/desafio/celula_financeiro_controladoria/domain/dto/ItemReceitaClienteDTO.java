package br.com.desafio.celula_financeiro_controladoria.domain.dto;

import java.math.BigDecimal;

public record ItemReceitaClienteDTO(
        Long clienteId,
        String cliente,
        long quantidadeMov,
        BigDecimal valor) {
}
