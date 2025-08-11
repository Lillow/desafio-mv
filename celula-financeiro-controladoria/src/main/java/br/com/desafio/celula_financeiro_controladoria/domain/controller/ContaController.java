package br.com.desafio.celula_financeiro_controladoria.domain.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.Conta;
import br.com.desafio.celula_financeiro_controladoria.domain.service.ContaService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ContaController {

    @Autowired
    private ContaService contaService;

    @PostMapping("/clientes/{clienteId}/contas")
    public ResponseEntity<Conta> criar(@PathVariable Long clienteId, @RequestBody Conta conta) {
        Conta criada = contaService.criar(clienteId, conta);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping("/clientes/{clienteId}/contas")
    public ResponseEntity<List<Conta>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(contaService.listarPorCliente(clienteId));
    }

    // GET /contas/{id}
    @GetMapping("/contas/{id}")
    public ResponseEntity<Conta> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(contaService.buscarPorId(id));
    }

    @PutMapping("/contas/{id}")
    public ResponseEntity<Conta> atualizar(@PathVariable Long id, @RequestBody Conta req) {
        return ResponseEntity.ok(contaService.atualizar(id, req));
    }

    @DeleteMapping("/contas/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        contaService.excluirLogico(id);
        return ResponseEntity.noContent().build();
    }
}