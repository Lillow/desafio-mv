package br.com.desafio.celula_financeiro_controladoria.domain.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.Conta;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.Cliente;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.StatusConta;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.ContaRepository;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.cliente.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContaService {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Transactional
    public Conta criar(Long clienteId, Conta conta) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        conta.setCliente(cliente);
        conta.setAtivo(Boolean.TRUE);
        if (conta.getSaldo() == null)
            conta.setSaldo(new BigDecimal("0.00"));
        if (conta.getStatus() == null)
            conta.setStatus(StatusConta.ATIVA);

        return contaRepository.saveAndFlush(conta);
    }

    @Transactional(readOnly = true)
    public List<Conta> listarPorCliente(Long clienteId) {
        return contaRepository.findByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public Conta buscarPorId(Long id) {
        return contaRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    @Transactional
    public Conta atualizar(Long id, Conta dados) {
        Conta conta = buscarPorId(id);

        // Regra: se há movimentação, não pode alterar
        if (contaRepository.existsMovimentoByContaId(id)) {
            throw new IllegalStateException("Conta possui movimentações e não pode ser alterada.");
        }

        // Campos atualizáveis
        if (dados.getAgencia() != null)
            conta.setAgencia(dados.getAgencia());
        if (dados.getNumero() != null)
            conta.setNumero(dados.getNumero());
        if (dados.getDocumento() != null)
            conta.setDocumento(dados.getDocumento());
        if (dados.getTipoConta() != null)
            conta.setTipoConta(dados.getTipoConta());
        if (dados.getStatus() != null)
            conta.setStatus(dados.getStatus());

        // saldo não deve ser alterado diretamente aqui (mantido pelas movimentações)
        return contaRepository.saveAndFlush(conta);
    }

    @Transactional
    public void excluirLogico(Long id) {
        Conta conta = buscarPorId(id);
        conta.setAtivo(false);
        contaRepository.saveAndFlush(conta);
    }
}