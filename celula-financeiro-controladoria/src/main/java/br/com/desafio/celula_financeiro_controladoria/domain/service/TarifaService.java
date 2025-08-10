package br.com.desafio.celula_financeiro_controladoria.domain.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

@Service
public class TarifaService {
    public BigDecimal tarifaPorQuantidade(long qtd) {
        if (qtd <= 10)
            return BigDecimal.valueOf(1.00);
        if (qtd <= 20)
            return BigDecimal.valueOf(0.75);
        return BigDecimal.valueOf(0.50);
    }

    public BigDecimal calcularValorTotal(long qtd) {
        return tarifaPorQuantidade(qtd).multiply(BigDecimal.valueOf(qtd));
    }
}
