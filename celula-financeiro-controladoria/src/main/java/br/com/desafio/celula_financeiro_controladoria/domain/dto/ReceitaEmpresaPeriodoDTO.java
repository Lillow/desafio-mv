package br.com.desafio.celula_financeiro_controladoria.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReceitaEmpresaPeriodoDTO(
        LocalDate inicio,
        LocalDate fim,
        List<ReceitaClientePeriodoDTO> clientes,
        BigDecimal total) {
}
