package br.com.desafio.celula_financeiro_controladoria.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.base.BaseEntityDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoMovimento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MovimentoDTO extends BaseEntityDTO {
    @NotNull
    private Long contaId;
    @NotNull
    private TipoMovimento tipo;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal valor;
    private String descricao;
    private String origem;
    private LocalDateTime dataHora; // se null, usar now()
}
