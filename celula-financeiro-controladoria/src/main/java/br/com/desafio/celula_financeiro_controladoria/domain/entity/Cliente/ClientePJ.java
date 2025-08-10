package br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.ClienteDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoPessoa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CLIENTE_PJ")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientePJ extends Cliente {

    @Size(min = 14, max = 14, message = "CNPJ deve ter 14 dígitos")
    @Pattern(regexp = "\\d{14}", message = "CNPJ deve conter apenas números")
    @Column(name = "CNPJ", length = 14, nullable = false, unique = true)
    private String cnpj;

    @Column(name = "RAZAO_SOCIAL", length = 150, nullable = false)
    private String razaoSocial;

    @Column(name = "INSCR_ESTADUAL", length = 20)
    private String inscEstadual;

    public ClientePJ(ClienteDTO dto) {
        super(dto);
        this.cnpj = dto.getCnpj();
        this.razaoSocial = dto.getRazaoSocial();
        this.inscEstadual = dto.getInscEstadual();
    }

    public ClienteDTO toDTO() {
        ClienteDTO dto = new ClienteDTO();
        super.toDTO(dto);

        dto.setTipoPessoa(TipoPessoa.PJ);
        dto.setCnpj(this.cnpj);
        dto.setRazaoSocial(this.razaoSocial);
        dto.setInscEstadual(this.inscEstadual);

        dto.setCpf(null);
        dto.setRg(null);
        dto.setDataNasc(null);

        return dto;
    }

}
