package br.com.desafio.celula_financeiro_controladoria.domain.controller;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.RelatorioClienteDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.dto.RelatorioReceitaDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.service.RelatorioService;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    @GetMapping("/cliente/{clienteId}")
    public RelatorioClienteDTO saldoCliente(@PathVariable Long clienteId) {
        return relatorioService.saldoCliente(clienteId);
    }

    @GetMapping("/cliente/{clienteId}/periodo")
    public RelatorioClienteDTO saldoClientePeriodo(@PathVariable Long clienteId,
            @RequestParam OffsetDateTime ini,
            @RequestParam OffsetDateTime fim) {
        return relatorioService.saldoClientePeriodo(clienteId, ini, fim);
    }

    @GetMapping("/saldos")
    public List<RelatorioClienteDTO.ResumoSaldoClienteDTO> saldosTodos(@RequestParam OffsetDateTime data) {
        return relatorioService.saldosTodosClientesNaData(data);
    }

    @GetMapping("/receita")
    public RelatorioReceitaDTO receita(@RequestParam OffsetDateTime ini,
            @RequestParam OffsetDateTime fim) {
        return relatorioService.receitaPeriodo(ini, fim);
    }
}
