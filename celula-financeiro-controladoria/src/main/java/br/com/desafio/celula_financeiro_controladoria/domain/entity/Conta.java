package br.com.desafio.celula_financeiro_controladoria.domain.entity;

import java.math.BigDecimal;

import org.hibernate.annotations.Check;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.ContaDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.base.BaseEntity;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.Cliente;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.StatusConta;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoConta;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoMovimentacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CONTA", uniqueConstraints = {
        @UniqueConstraint(name = "UK_CONTA_AG_NUM", columnNames = { "AGENCIA", "NUMERO" })
})
@Check(constraints = "SALDO >= 0")
@NoArgsConstructor
public class Conta extends BaseEntity {

    @Column(name = "DOCUMENTO", nullable = false, length = 14) // 11 (CPF) ou 14 (CNPJ)
    private String documento;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_CONTA", nullable = false, length = 12)
    private TipoConta tipoConta;

    @Column(name = "AGENCIA", nullable = false, length = 10)
    private String agencia;

    @Column(name = "NUMERO", nullable = false, length = 20)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 10)
    private StatusConta status;

    @Column(name = "SALDO", nullable = false, precision = 20, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    @jakarta.persistence.ManyToOne(optional = false)
    @jakarta.persistence.JoinColumn(name = "CLIENTE_ID", nullable = false)
    private Cliente cliente;

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
