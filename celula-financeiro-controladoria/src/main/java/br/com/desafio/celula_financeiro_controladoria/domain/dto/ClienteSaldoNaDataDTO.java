package br.com.desafio.celula_financeiro_controladoria.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClienteSaldoNaDataDTO(
        String cliente,
        LocalDate clienteDesde,
        LocalDate dataRef,
        BigDecimal saldo) {
}
