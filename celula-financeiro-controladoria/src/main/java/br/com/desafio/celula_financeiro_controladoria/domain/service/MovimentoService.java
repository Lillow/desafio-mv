package br.com.desafio.celula_financeiro_controladoria.domain.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.MovimentoDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.Conta;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.Movimento;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoMovimento;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.ContaRepository;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.MovimentoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovimentoService {

    private final ContaRepository contaRepo;
    private final MovimentoRepository movRepo;

    @Transactional
    public Movimento registrarMovimento(MovimentoDTO dto) {
        Conta conta = contaRepo.findById(dto.getContaId())
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));

        Movimento m = new Movimento();
        m.setConta(conta);
        m.setTipo(dto.getTipo());
        m.setValor(dto.getValor());
        m.setDescricao(dto.getDescricao());
        m.setOrigem(dto.getOrigem());
        m.setDataHora(Optional.ofNullable(dto.getDataHora()).orElse(LocalDateTime.now()));

        // aplica no saldo
        BigDecimal novoSaldo = conta.getSaldo();
        if (dto.getTipo() == TipoMovimento.CREDITO) {
            novoSaldo = novoSaldo.add(dto.getValor());
        } else {
            // valida saldo >= 0 (regra já tem check DB, mas validamos aqui tb)
            if (novoSaldo.compareTo(dto.getValor()) < 0) {
                throw new IllegalStateException("Saldo insuficiente");
            }
            novoSaldo = novoSaldo.subtract(dto.getValor());
        }
        conta.setSaldo(novoSaldo);

        // persiste por cascata ou explicitamente
        return movRepo.save(m);
    }

    public List<Movimento> listarPorContaPeriodo(Long contaId, LocalDate ini, LocalDate fim) {
        return movRepo.findByContaAndPeriodo(contaId, ini.atStartOfDay(), fim.atTime(23, 59, 59));
    }
}
