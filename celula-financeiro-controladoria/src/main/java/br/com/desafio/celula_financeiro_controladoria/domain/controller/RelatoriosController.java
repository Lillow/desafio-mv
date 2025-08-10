package br.com.desafio.celula_financeiro_controladoria.domain.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.ReceitaClienteDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.dto.RelatorioSaldoDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.service.RelatorioService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatoriosController {

    private final RelatorioService relatorioService;

    @GetMapping("/saldo/{clienteId}")
    public RelatorioSaldoDTO saldoCliente(
            @PathVariable Long clienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ini,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return relatorioService.saldoCliente(clienteId, ini, fim);
    }

    @GetMapping("/saldo-todos")
    public List<Map<String, Object>> saldoTodos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataRef) {
        return // para cada cliente: {cliente, clienteDesde, saldoEmData}
               // pode usar relatorioService.saldoCliente(clienteId, null, dataRef) e extrair
               // saldoAtual
        List.of();
    }

    @GetMapping("/receita")
    public Map<String, Object> receita(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ini,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        var lista = relatorioService.receitaPorPeriodo(ini, fim);
        BigDecimal total = lista.stream().map(ReceitaClienteDTO::getValorTarifado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of("periodo", Map.of("inicio", ini, "fim", fim),
                "clientes", lista,
                "totalReceitas", total);
    }
}
