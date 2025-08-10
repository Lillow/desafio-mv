package br.com.desafio.celula_financeiro_controladoria.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.Conta;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.ContaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContaService {

    private final ContaRepository contaRepo;

    @Transactional
    public Conta criar(Conta c) {
        return contaRepo.save(c);
    }

    @Transactional
    public Conta atualizar(Conta atualizada) {
        Conta db = contaRepo.findById(atualizada.getId())
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));
        // regra: se existir movimentação associada, não permitir alteração de campos
        // sensíveis
        if (!db.getMovimentos().isEmpty()) {
            // permitir talves apenas status; negar agência/numero/documento/tipoConta
            db.setStatus(atualizada.getStatus());
            return db;
        }
        db.setAgencia(atualizada.getAgencia());
        db.setNumero(atualizada.getNumero());
        db.setDocumento(atualizada.getDocumento());
        db.setTipoConta(atualizada.getTipoConta());
        db.setStatus(atualizada.getStatus());
        return db;
    }

    @Transactional
    public void excluirLogicamente(Long id) {
        // @SQLDelete já faz ativo=false
        contaRepo.deleteById(id);
    }

    public List<Conta> listarDoCliente(Long clienteId) {
        return contaRepo.findByClienteId(clienteId);
    }
}
