package br.com.desafio.celula_financeiro_controladoria.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.Conta;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.Movimento;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoMovimento;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.ContaRepository;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.MovimentoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class MovimentoService {

    @Autowired
    private MovimentoRepository movimentoRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Transactional
    public Movimento registrar(Long contaId, Movimento mov) {
        Conta conta = contaRepository.findById(contaId).orElseThrow(EntityNotFoundException::new);

        mov.setConta(conta);
        if (mov.getDataHora() == null)
            mov.setDataHora(LocalDateTime.now());

        Movimento saved = movimentoRepository.save(mov);

        // Atualiza saldo
        BigDecimal saldo = conta.getSaldo();
        if (mov.getTipo() == TipoMovimento.CREDITO) {
            saldo = saldo.add(mov.getValor());
        } else {
            saldo = saldo.subtract(mov.getValor());
            if (saldo.signum() < 0)
                throw new IllegalStateException("Saldo não pode ficar negativo.");
        }
        conta.setSaldo(saldo);
        contaRepository.save(conta);

        return saved;
    }
}
