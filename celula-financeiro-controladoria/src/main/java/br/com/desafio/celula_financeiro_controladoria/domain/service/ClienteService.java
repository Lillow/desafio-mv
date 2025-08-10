package br.com.desafio.celula_financeiro_controladoria.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.Cliente;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.cliente.ClienteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepo;

    @Transactional
    public Cliente atualizar(Cliente in) {
        Cliente db = clienteRepo.findById(in.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
        // imutáveis
        // if (db instanceof ClientePF pfDb && in instanceof ClientePF pfIn) {
        // pfIn.setCpf(pfDb.getCpf()); }
        // copiar apenas campos permitidos
        db.setNome(in.getNome());
        db.setEmail(in.getEmail());
        db.setTelefone(in.getTelefone());
        return db;
    }
}
