package br.com.desafio.celula_financeiro_controladoria.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.MovimentoDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.dto.base.BaseEntityDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.base.BaseEntity;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoMovimento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movimento")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Movimento extends BaseEntity { // supondo que vc já tem createdAt/updatedAt/ativo
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_id", nullable = false)
    private Conta conta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoMovimento tipo; // CREDITO | DEBITO

    @Column(name = "valor", nullable = false, precision = 20, scale = 2)
    private BigDecimal valor;

    @Column(name = "descricao", length = 200)
    private String descricao;

    @Column(name = "origem", length = 60) // p/ simular integração (banco emissor etc.)
    private String origem;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Override
    public MovimentoDTO toDTO() {
        MovimentoDTO dto = new MovimentoDTO();
        super.toDTO(dto);

        dto.setTipo(this.getTipo());
        dto.setValor(this.getValor());
        dto.setDescricao(this.getDescricao());
        dto.setOrigem(this.getOrigem());
        dto.setDataHora(this.getDataHora());

        return dto;
    }
}
