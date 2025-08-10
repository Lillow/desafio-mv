package br.com.desafio.celula_financeiro_controladoria.domain.service;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.EnderecoDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.Endereco;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.Cliente;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.EnderecoRepository;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.cliente.ClienteRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnderecoService {

    private final EnderecoRepository enderecoRepo;
    private final ClienteRepository clienteRepo;

    @Transactional
    public Endereco criar(Long clienteId, EnderecoDTO in) {
        Cliente cliente = clienteRepo.findById(clienteId)
                .orElseThrow(() -> new NoSuchElementException("Cliente não encontrado"));

        if (enderecoRepo.existsByClienteIdAndAtivoTrue(clienteId)) {
            throw new IllegalStateException("Cliente já possui endereço ativo");
        }

        Endereco e = new Endereco();
        e.setCliente(cliente); // imutável após criação
        e.setAtivo(true);
        copiarCamposPermitidos(e, in);
        return enderecoRepo.save(e);
    }

    @Transactional(readOnly = true)
    public Endereco buscarPorCliente(Long clienteId) {
        return enderecoRepo.findByClienteId(clienteId)
                .orElseThrow(() -> new NoSuchElementException("Endereço não encontrado para cliente"));
    }

    @Transactional(readOnly = true)
    public Endereco buscarPorId(Long id) {
        return enderecoRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Endereço não encontrado"));
    }

    @Transactional
    public Endereco atualizar(Long id, EnderecoDTO in) {
        Endereco db = enderecoRepo.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new NoSuchElementException("Endereço não encontrado/ativo"));
        // imutáveis: id e cliente
        copiarCamposPermitidos(db, in);
        return db; // JPA dirty checking
    }

    @Transactional
    public void removerLogico(Long id) {
        Endereco db = enderecoRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Endereço não encontrado"));
        db.setAtivo(false);
    }

    private void copiarCamposPermitidos(Endereco e, EnderecoDTO in) {
        e.setLogradouro(in.getLogradouro());
        e.setNumero(in.getNumero());
        e.setComplemento(in.getComplemento());
        e.setBairro(in.getBairro());
        e.setCidade(in.getCidade());
        e.setUf(in.getUf());
        e.setCep(in.getCep());
    }
}
