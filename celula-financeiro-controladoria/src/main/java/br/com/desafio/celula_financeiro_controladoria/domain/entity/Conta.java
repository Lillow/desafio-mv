package br.com.desafio.celula_financeiro_controladoria.domain.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.fasterxml.jackson.annotation.JsonIgnore;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.ContaDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.base.BaseEntity;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.Cliente;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.StatusConta;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoConta;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoMovimento;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "conta", uniqueConstraints = @UniqueConstraint(name = "UK_CONTA_AG_NUM", columnNames = { "agencia",
        "numero" }))
@SQLDelete(sql = "update conta set ativo=false where id=?")
@Where(clause = "ativo = true")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Conta extends BaseEntity {

    @Column(nullable = false, length = 10)
    private String agencia;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(nullable = false, length = 14)
    private String documento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoConta tipoConta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConta status;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO.setScale(2); // garantir escala

    // Evitar remover movimentos por cascata; manter persist/merge.
    @OneToMany(mappedBy = "conta", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = false)
    @JsonIgnore // evite payload gigante/loop; exponha via DTO quando precisar
    private List<Movimento> movimentos = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnore
    private Cliente cliente;

    public Conta(ContaDTO dto) {
        super(dto);
        this.documento = dto.getDocumento();
        this.tipoConta = dto.getTipoConta();
        this.agencia = dto.getAgencia();
        this.numero = dto.getNumero();
        this.status = dto.getStatus();
        this.saldo = (dto.getSaldo() != null ? dto.getSaldo() : BigDecimal.ZERO).setScale(2);
    }

    public ContaDTO toDTO() {
        ContaDTO dto = new ContaDTO();
        super.toDTO(dto);
        dto.setDocumento(this.documento);
        dto.setTipoConta(this.tipoConta);
        dto.setAgencia(this.agencia);
        dto.setNumero(this.numero);
        dto.setStatus(this.status);
        dto.setSaldo(this.saldo);
        dto.setCliente(this.cliente != null ? this.cliente.toDTO() : null);
        return dto;
    }

    /** Helper para manter o vínculo bidirecional corretamente. */
    public void addMovimento(Movimento m) {
        if (m == null)
            return;
        m.setConta(this);
        this.movimentos.add(m);
    }

    /** Aplica uma movimentação ao saldo (regras mínimas desta etapa). */
    public void aplicarMovimentacao(TipoMovimento tipo, BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("Valor da movimentação deve ser positivo.");
        }
        valor = valor.setScale(2);
        switch (tipo) {
            case CREDITO -> this.saldo = this.saldo.add(valor);
            case DEBITO -> {
                BigDecimal novo = this.saldo.subtract(valor);
                if (novo.signum() < 0) {
                    throw new IllegalStateException("Saldo insuficiente para débito.");
                }
                this.saldo = novo;
            }
        }
        this.saldo = this.saldo.setScale(2);
    }
}