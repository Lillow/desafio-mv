package br.com.desafio.celula_financeiro_controladoria.domain.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.ItemReceitaClienteDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.dto.RelatorioClienteDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.dto.RelatorioReceitaDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.Conta;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.Cliente;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.MovimentoRepository;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.cliente.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class RelatorioService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MovimentoRepository movimentoRepository;

    // preço por movimentação conforme volume do cliente no período
    private BigDecimal precoPorMov(long qtd) {
        if (qtd <= 10)
            return new BigDecimal("1.00");
        if (qtd <= 20)
            return new BigDecimal("0.75");
        return new BigDecimal("0.50");
    }

    public RelatorioClienteDTO saldoCliente(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId).orElseThrow(EntityNotFoundException::new);

        BigDecimal cred = BigDecimal.ZERO;
        BigDecimal deb = BigDecimal.ZERO;
        List<Conta> contas = cliente.getContas();
        for (Conta c : contas) {
            cred = cred.add(movimentoRepository.sumCreditosByConta(c.getId()));
            deb = deb.add(movimentoRepository.sumDebitosByConta(c.getId()));
        }
        BigDecimal saldoInicial = BigDecimal.ZERO;
        BigDecimal saldoAtual = cred.subtract(deb); // ou somar saldos das contas, se preferir

        long qtd = 0;
        for (Conta c : contas)
            qtd += movimentoRepository.countByContaIdAndDataHoraBetween(c.getId(), OffsetDateTime.MIN,
                    OffsetDateTime.now());
        BigDecimal valorPago = precoPorMov(qtd).multiply(BigDecimal.valueOf(qtd));

        return RelatorioClienteDTO.from(cliente, cred, deb, qtd, valorPago, saldoInicial, saldoAtual, null, null);
    }

    public RelatorioClienteDTO saldoClientePeriodo(Long clienteId, OffsetDateTime ini, OffsetDateTime fim) {
        var cliente = clienteRepository.findById(clienteId).orElseThrow(EntityNotFoundException::new);
        BigDecimal cred = BigDecimal.ZERO, deb = BigDecimal.ZERO;
        long qtd = 0;
        for (var c : cliente.getContas()) {
            cred = cred.add(movimentoRepository.sumCreditosByContaAndPeriodo(c.getId(), ini, fim));
            deb = deb.add(movimentoRepository.sumDebitosByContaAndPeriodo(c.getId(), ini, fim));
            qtd += movimentoRepository.countByContaIdAndDataHoraBetween(c.getId(), ini, fim);
        }
        BigDecimal valorPago = precoPorMov(qtd).multiply(BigDecimal.valueOf(qtd));
        BigDecimal saldoInicial = BigDecimal.ZERO; // pode-se calcular trazendo acumulado anterior
        BigDecimal saldoAtual = cred.subtract(deb);

        return RelatorioClienteDTO.from(cliente, cred, deb, qtd, valorPago, saldoInicial, saldoAtual, ini, fim);
    }

    public List<RelatorioClienteDTO.ResumoSaldoClienteDTO> saldosTodosClientesNaData(OffsetDateTime data) {
        var all = clienteRepository.findAll();
        var out = new ArrayList<RelatorioClienteDTO.ResumoSaldoClienteDTO>();
        for (Cliente cli : all) {
            BigDecimal cred = BigDecimal.ZERO;
            BigDecimal deb = BigDecimal.ZERO;
            for (var c : cli.getContas()) {
                cred = cred.add(movimentoRepository.sumCreditosByContaAndPeriodo(c.getId(), OffsetDateTime.MIN, data));
                deb = deb.add(movimentoRepository.sumDebitosByContaAndPeriodo(c.getId(), OffsetDateTime.MIN, data));
            }
            out.add(new RelatorioClienteDTO.ResumoSaldoClienteDTO(cli.getId(), cli.getNome(),
                    java.time.LocalDateTime.ofInstant(cli.getCreatedAt(), java.time.ZoneId.systemDefault())
                            .toLocalDate(),
                    cred.subtract(deb)));
        }
        return out;
    }

    public RelatorioReceitaDTO receitaPeriodo(OffsetDateTime ini, OffsetDateTime fim) {
        var dados = movimentoRepository.countPorClienteNoPeriodo(ini, fim);
        List<ItemReceitaClienteDTO> itens = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Object[] row : dados) {
            Long clienteId = (Long) row[0];
            long qtd = (long) row[1];
            var cliente = clienteRepository.findById(clienteId).orElseThrow(EntityNotFoundException::new);
            BigDecimal unit = precoPorMov(qtd);
            BigDecimal valor = unit.multiply(BigDecimal.valueOf(qtd));
            total = total.add(valor);

            itens.add(new ItemReceitaClienteDTO(cliente.getId(), cliente.getNome(), qtd, valor));
        }
        return new RelatorioReceitaDTO(ini, fim, itens, total);
    }
}
