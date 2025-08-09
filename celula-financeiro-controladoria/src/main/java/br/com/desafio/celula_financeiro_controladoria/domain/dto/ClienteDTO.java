package br.com.desafio.celula_financeiro_controladoria.domain.dto;

import java.time.LocalDate;

import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoPessoa;
import lombok.Data;

@Data
public class ClienteDTO {

    private Long id;
    private String nome;
    private String cpf;
    private String cnpj;
    private String email;
    private String telefone;
    private String rg;
    private String razaoSocial;
    private String inscricaoEstadual;
    private LocalDate dataNascimento;
    private TipoPessoa tipoPessoa;
}
