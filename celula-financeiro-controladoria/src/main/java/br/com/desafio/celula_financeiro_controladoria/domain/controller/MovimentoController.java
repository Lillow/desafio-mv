package br.com.desafio.celula_financeiro_controladoria.domain.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.NovaMovimentacaoDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.Movimento;
import br.com.desafio.celula_financeiro_controladoria.domain.service.MovimentoService;

@RestController
@RequestMapping("/contas/{contaId}/movimentos")
public class MovimentoController {

    @Autowired
    private MovimentoService movimentoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Movimento criar(@PathVariable Long contaId, @RequestBody NovaMovimentacaoDTO dto) {
        Movimento m = new Movimento();
        m.setTipo(dto.tipo());
        m.setValor(dto.valor());
        m.setDescricao(dto.descricao());
        m.setOrigem(dto.origem());
        m.setDataHora(dto.dataHora().toLocalDateTime());
        return movimentoService.registrar(contaId, m);
    }
}
