package br.com.desafio.celula_financeiro_controladoria.domain.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.ClienteSaldoNaDataDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.dto.ReceitaEmpresaPeriodoDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.dto.RelatorioSaldoClienteDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.dto.RelatorioSaldoClientePeriodoDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.service.RelatorioService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    @GetMapping("/{clienteId}/relatorios/saldo")
    public ResponseEntity<RelatorioSaldoClienteDTO> relatorioSaldo(@PathVariable Long clienteId) {
        return ResponseEntity.ok(relatorioService.saldoCliente(clienteId));
    }

    @GetMapping("/{clienteId}/relatorios/saldo-periodo")
    public ResponseEntity<RelatorioSaldoClientePeriodoDTO> relatorioSaldoPeriodo(
            @PathVariable Long clienteId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(relatorioService.saldoClientePeriodo(clienteId, from, to));
    }

    @GetMapping("/relatorios/saldos-clientes")
    public ResponseEntity<List<ClienteSaldoNaDataDTO>> saldosClientesEm(
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(relatorioService.saldoTodosClientesNaData(data));
    }

    @GetMapping("/relatorios/receita")
    public ResponseEntity<ReceitaEmpresaPeriodoDTO> receita(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(relatorioService.receitaEmpresaPeriodo(from, to));
    }
}
