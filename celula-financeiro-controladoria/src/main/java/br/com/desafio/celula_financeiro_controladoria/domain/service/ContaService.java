package br.com.desafio.celula_financeiro_controladoria.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.Conta;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.ContaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ContaService {

    @Autowired
    private ContaRepository contaRepository;

    @Transactional
    public Conta criar(Conta conta) {
        conta.setAtivo(true);
        return contaRepository.save(conta);
    }

    @Transactional
    public Conta atualizar(Long id, Conta dados) {
        Conta conta = contaRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (contaRepository.existsMovimentoByContaId(id)) {
            throw new IllegalStateException("Conta possui movimentações e não pode ser alterada.");
        }
        conta.setAgencia(dados.getAgencia());
        conta.setNumero(dados.getNumero());
        conta.setDocumento(dados.getDocumento());
        conta.setTipoConta(dados.getTipoConta());
        // saldo e status podem ter regras próprias; ajuste se necessário
        return contaRepository.save(conta);
    }

    @Transactional
    public void excluirLogico(Long id) {
        Conta conta = contaRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        conta.setAtivo(false);
        contaRepository.save(conta);
    }
}