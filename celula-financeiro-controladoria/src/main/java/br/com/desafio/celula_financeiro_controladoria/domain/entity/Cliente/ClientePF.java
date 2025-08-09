package br.com.desafio.celula_financeiro_controladoria.domain.entity.Cliente;

import java.time.LocalDate;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.ClienteDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoPessoa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CLIENTE_PF")
@NoArgsConstructor
public class ClientePF extends Cliente {

    @Size(min = 11, max = 11, message = "CPF deve ter 11 dígitos")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter apenas números")
    @Column(name = "CPF", length = 11, nullable = false, unique = true)
    private String cpf;

    @Column(name = "RG", length = 15)
    private String rg;

    @Column(name = "DATA_NASC")
    private LocalDate dataNascimento;

    public ClientePF(ClienteDTO dto) {
        super(dto);
        this.cpf = dto.getCpf();
        this.rg = dto.getRg();
        this.dataNascimento = dto.getDataNascimento();
    }

    @Override
    public ClienteDTO toDTO() {
        ClienteDTO dto = new ClienteDTO();
        super.toDTO(dto);
        dto.setTipoPessoa(TipoPessoa.PF);
        dto.setCpf(this.cpf);
        dto.setRg(this.rg);
        dto.setDataNascimento(this.dataNascimento);

        dto.setCnpj(null);
        dto.setRazaoSocial(null);
        dto.setInscricaoEstadual(null);
        return dto;
    }
}
