package br.com.desafio.celula_financeiro_controladoria.domain.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.ContaDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.base.BaseEntity;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.Cliente;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.StatusConta;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoConta;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoMovimentacao;
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

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

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
    private BigDecimal saldo = BigDecimal.ZERO;

    @OneToMany(mappedBy = "conta", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Movimento> movimentos = new ArrayList<>();

    public Conta(ContaDTO dto) {
        super(dto);
        this.documento = dto.getDocumento();
        this.tipoConta = dto.getTipoConta();
        this.agencia = dto.getAgencia();
        this.numero = dto.getNumero();
        this.status = dto.getStatus();
        this.saldo = dto.getSaldo() != null ? dto.getSaldo() : BigDecimal.ZERO;
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

    /**
     * Aplica uma movimentação ao saldo (em memória nesta etapa).
     * Em etapas futuras, isso estará dentro de uma transação e registrará
     * histórico.
     */
    public void aplicarMovimentacao(TipoMovimentacao tipo, BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("Valor da movimentação deve ser positivo.");
        }
        switch (tipo) {
            case CREDITO -> this.saldo = this.saldo.add(valor);
            case DEBITO -> {
                BigDecimal novo = this.saldo.subtract(valor);
                if (novo.signum() < 0) {
                    // Regra mínima para a etapa: não permitir negativo
                    throw new IllegalStateException("Saldo insuficiente para débito.");
                }
                this.saldo = novo;
            }
        }
    }
}
