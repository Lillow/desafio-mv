package br.com.desafio.celula_financeiro_controladoria.domain.entity;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.EnderecoDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.base.BaseEntity;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.Cliente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ENDERECO")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Endereco extends BaseEntity {

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

    @Setter
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "CLIENTE_ID", nullable = false, unique = true, foreignKey = @ForeignKey(name = "FK_ENDERECO_CLIENTE"))
    private Cliente cliente;

    public Endereco(EnderecoDTO dto) {
        super(dto);
        this.logradouro = dto.getLogradouro();
        this.numero = dto.getNumero();
        this.bairro = dto.getBairro();
        this.cidade = dto.getCidade();
        this.uf = dto.getUf();
        this.cep = dto.getCep();
        this.complemento = dto.getComplemento();
    }

    public EnderecoDTO toDTO() {
        EnderecoDTO dto = new EnderecoDTO();
        super.toDTO(dto);
        dto.setLogradouro(logradouro);
        dto.setNumero(numero);
        dto.setBairro(bairro);
        dto.setCidade(cidade);
        dto.setUf(uf);
        dto.setCep(cep);
        dto.setComplemento(complemento);

        return dto;
    }

    public static EnderecoDTO from(Endereco e) {
        if (e == null)
            return null;
        return e.toDTO();
    }
}
