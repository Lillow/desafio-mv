package br.com.desafio.celula_financeiro_controladoria.domain.entity;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.Cliente.Cliente;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "ENDERECO")
public class Endereco extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "CLIENTE_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_ENDERECO_CLIENTE"))
    private Cliente cliente;

    @NotBlank
    @Size(max = 120)
    @Column(name = "LOGRADOURO", length = 120, nullable = false)
    private String logradouro;

    @NotBlank
    @Size(max = 20)
    @Column(name = "NUMERO", length = 20, nullable = false)
    private String numero;

    @Size(max = 60)
    @Column(name = "BAIRRO", length = 60)
    private String bairro;

    @Size(max = 60)
    @Column(name = "CIDADE", length = 60)
    private String cidade;

    @Size(max = 2)
    @Column(name = "UF", length = 2)
    private String uf;

    @Size(max = 8)
    @Column(name = "CEP", length = 8)
    private String cep;

    @Size(max = 120)
    @Column(name = "COMPLEMENTO", length = 120)
    private String complemento;
}
