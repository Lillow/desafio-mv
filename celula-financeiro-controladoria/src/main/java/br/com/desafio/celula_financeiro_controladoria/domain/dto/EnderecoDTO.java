package br.com.desafio.celula_financeiro_controladoria.domain.dto;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.base.BaseEntityDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EnderecoDTO extends BaseEntityDTO {

    private String logradouro;
    private String numero;
    private String bairro;
    private String cidade;
    private String uf;
    private String cep;
    private String complemento;
    private ClienteDTO cliente;
}
