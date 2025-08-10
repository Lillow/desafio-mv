package br.com.desafio.celula_financeiro_controladoria.domain.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record RelatorioReceitaDTO(
        OffsetDateTime periodoIni,
        OffsetDateTime periodoFim,
        List<ItemReceitaClienteDTO> itens,
        BigDecimal total) {
}
