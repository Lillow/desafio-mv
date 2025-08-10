package br.com.desafio.celula_financeiro_controladoria.domain.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.EnderecoDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.Endereco;
import br.com.desafio.celula_financeiro_controladoria.domain.service.EnderecoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class EnderecoController {

    private final EnderecoService service;

    @PostMapping("/clientes/{clienteId}/enderecos")
    public ResponseEntity<Endereco> criar(@PathVariable Long clienteId,
            @RequestBody @Validated EnderecoDTO dto) {
        Endereco saved = service.criar(clienteId, dto);
        return ResponseEntity
                .created(URI.create("/enderecos/" + saved.getId()))
                .body(saved);
    }

    @GetMapping("/clientes/{clienteId}/enderecos")
    public ResponseEntity<Endereco> buscarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(service.buscarPorCliente(clienteId));
    }

    @GetMapping("/enderecos/{id}")
    public ResponseEntity<Endereco> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/enderecos/{id}")
    public ResponseEntity<Endereco> atualizar(@PathVariable Long id,
            @RequestBody @Validated EnderecoDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/enderecos/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.removerLogico(id);
        return ResponseEntity.noContent().build();
    }
}
