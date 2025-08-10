package br.com.desafio.celula_financeiro_controladoria.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.Cliente;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.ClientePF;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.ClientePJ;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.cliente.ClientePFRepository;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.cliente.ClientePJRepository;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.cliente.ClienteRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepo;
    private final ClientePFRepository clientePFRepo;
    private final ClientePJRepository clientePJRepo;

    // ---------- Comuns ----------
    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteRepo.findAll();
    }

    @Transactional
    public void inativarCliente(Long id) {
        Cliente db = buscarPorId(id);
        db.setAtivo(false);
        // se quiser garantir flush imediato:
        clienteRepo.save(db);
    }

    // ---------- PF ----------
    @Transactional
    public ClientePF salvarPF(ClientePF in) {
        in.setId(null); // garante insert
        return clientePFRepo.save(in);
    }

    @Transactional
    public ClientePF atualizarPF(Long id, ClientePF in) {
        ClientePF db = (ClientePF) buscarPorId(id);

        if (!(db instanceof ClientePF)) {
            throw new IllegalArgumentException("Cliente não é PF");
        }

        // regra de imutabilidade do CPF
        if (in.getCpf() != null && !in.getCpf().equals(((ClientePF) db).getCpf())) {
            throw new IllegalArgumentException("CPF não pode ser alterado");
        }

        copiarCamposEditaveis(db, in);
        // campos específicos PF (mutáveis)
        db.setDataNasc(in.getDataNasc());
        db.setRg(in.getRg());

        return clientePFRepo.save(db);
    }

    // ---------- PJ ----------
    @Transactional
    public ClientePJ salvarPJ(ClientePJ in) {
        in.setId(null); // garante insert
        return clientePJRepo.save(in);
    }

    @Transactional
    public ClientePJ atualizarPJ(Long id, ClientePJ in) {
        ClientePJ db = (ClientePJ) buscarPorId(id);

        if (!(db instanceof ClientePJ)) {
            throw new IllegalArgumentException("Cliente não é PJ");
        }

        // regra de imutabilidade do CNPJ
        if (in.getCnpj() != null && !in.getCnpj().equals(((ClientePJ) db).getCnpj())) {
            throw new IllegalArgumentException("CNPJ não pode ser alterado");
        }

        copiarCamposEditaveis(db, in);
        // campos específicos PJ (mutáveis)
        db.setRazaoSocial(in.getRazaoSocial());
        db.setInscEstadual(in.getInscEstadual());
        return clientePJRepo.save(db);
    }

    // ---------- Atualização genérica (se quiser manter) ----------
    @Transactional
    public Cliente atualizar(Cliente in) {
        Cliente db = clienteRepo.findById(in.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        // se PF, não permitir trocar CPF
        if (db instanceof ClientePF pfDb && in instanceof ClientePF pfIn) {
            if (pfIn.getCpf() != null && !pfIn.getCpf().equals(pfDb.getCpf())) {
                throw new IllegalArgumentException("CPF não pode ser alterado");
            }
            pfDb.setDataNasc(pfIn.getDataNasc());
            pfDb.setRg(pfIn.getRg());
        }

        // se PJ, não permitir trocar CNPJ
        if (db instanceof ClientePJ pjDb && in instanceof ClientePJ pjIn) {
            if (pjIn.getCnpj() != null && !pjIn.getCnpj().equals(pjDb.getCnpj())) {
                throw new IllegalArgumentException("CNPJ não pode ser alterado");
            }
            pjDb.setRazaoSocial(pjIn.getRazaoSocial());
            pjDb.setInscEstadual(pjIn.getInscEstadual());
        }

        copiarCamposEditaveis(db, in);
        return clienteRepo.save(db);
    }

    // ---------- helpers ----------
    private void copiarCamposEditaveis(Cliente destino, Cliente origem) {
        destino.setNome(origem.getNome());
        destino.setEmail(origem.getEmail());
        destino.setTelefone(origem.getTelefone());
        // NÃO mexe em: id, createdAt, updatedAt (gerenciados), ativo (salvo por
        // inativação)
    }
}
