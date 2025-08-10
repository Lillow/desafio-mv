package br.com.desafio.celula_financeiro_controladoria.domain.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.RelatorioSaldoClienteDTO;
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
}
