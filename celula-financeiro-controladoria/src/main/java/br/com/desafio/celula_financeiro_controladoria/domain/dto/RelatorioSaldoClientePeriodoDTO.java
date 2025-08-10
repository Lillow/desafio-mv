package br.com.desafio.celula_financeiro_controladoria.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RelatorioSaldoClientePeriodoDTO(
                LocalDate periodoInicio,
                LocalDate periodoFim,
                String cliente,
                LocalDate clienteDesde,
                String endereco,
                long movsCredito,
                long movsDebito,
                long totalMovs,
                BigDecimal valorPagoMovimentacoes,
                BigDecimal saldoInicial,
                BigDecimal saldoAtual) {
}
