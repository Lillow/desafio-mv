package br.com.desafio.celula_financeiro_controladoria.domain.dto;

import java.time.LocalDate;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.base.BaseEntityDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoPessoa;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClienteDTO extends BaseEntityDTO {

    private String nome;
    private String cpf;
    private String cnpj;
    private String email;
    private String telefone;
    private String rg;
    private String razaoSocial;
    private String inscEstadual;
    private LocalDate dataNasc;
    private TipoPessoa tipoPessoa;

    private EnderecoDTO endereco;
}
