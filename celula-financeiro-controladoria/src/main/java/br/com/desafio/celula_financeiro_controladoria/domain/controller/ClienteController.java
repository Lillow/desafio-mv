package br.com.desafio.celula_financeiro_controladoria.domain.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.Cliente;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.ClientePF;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.ClientePJ;
import br.com.desafio.celula_financeiro_controladoria.domain.service.ClienteService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    // --------- comuns ----------
    @GetMapping
    public ResponseEntity<List<Cliente>> listar() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        clienteService.inativarCliente(id);
        return ResponseEntity.noContent().build();
    }

    // --------- PF ----------
    @PostMapping("/pf")
    public ResponseEntity<ClientePF> criarPF(@RequestBody ClientePF body) {
        ClientePF salvo = clienteService.salvarPF(body);
        return ResponseEntity.created(URI.create("/api/clientes/" + salvo.getId())).body(salvo);
    }

    @PutMapping("/pf/{id}")
    public ResponseEntity<ClientePF> atualizarPF(@PathVariable Long id, @RequestBody ClientePF body) {
        ClientePF atualizado = clienteService.atualizarPF(id, body);
        return ResponseEntity.ok(atualizado);
    }

    // --------- PJ ----------
    @PostMapping("/pj")
    public ResponseEntity<ClientePJ> criarPJ(@RequestBody ClientePJ body) {
        ClientePJ salvo = clienteService.salvarPJ(body);
        return ResponseEntity.created(URI.create("/api/clientes/" + salvo.getId())).body(salvo);
    }

    @PutMapping("/pj/{id}")
    public ResponseEntity<ClientePJ> atualizarPJ(@PathVariable Long id, @RequestBody ClientePJ body) {
        ClientePJ atualizado = clienteService.atualizarPJ(id, body);
        return ResponseEntity.ok(atualizado);
    }
}
