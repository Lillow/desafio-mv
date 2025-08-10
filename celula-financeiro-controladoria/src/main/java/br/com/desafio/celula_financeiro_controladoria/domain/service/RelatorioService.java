package br.com.desafio.celula_financeiro_controladoria.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.RelatorioSaldoClienteDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.dto.RelatorioSaldoClientePeriodoDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.Endereco;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.Movimento;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.Cliente;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoMovimento;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.ContaRepository;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.EnderecoRepository;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.MovimentoRepository;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.cliente.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RelatorioService {

        private final ClienteRepository clienteRepo;
        private final EnderecoRepository enderecoRepo;
        private final MovimentoRepository movimentoRepo;

        @Transactional(readOnly = true)
        public RelatorioSaldoClienteDTO saldoCliente(Long clienteId) {
                Cliente cliente = clienteRepo.findById(clienteId)
                                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

                Endereco end = enderecoRepo.findByClienteId(clienteId).orElse(null);

                String enderecoFmt = end == null ? "—"
                                : String.format("%s, %s%s, %s, %s, %s, %s",
                                                nullToEmpty(end.getLogradouro()),
                                                nullToEmpty(end.getNumero()),
                                                end.getComplemento() == null || end.getComplemento().isBlank()
                                                                ? ""
                                                                : " - " + end.getComplemento(),
                                                nullToEmpty(end.getBairro()),
                                                nullToEmpty(end.getCidade()),
                                                nullToEmpty(end.getUf()),
                                                nullToEmpty(end.getCep()));

                // List<Conta> contas = contaRepo.findByClienteId(clienteId);
                List<Movimento> movs = movimentoRepo.findByClienteId(clienteId);

                long qtdCred = movs.stream().filter(m -> m.getTipo() == TipoMovimento.CREDITO).count();
                long qtdDeb = movs.stream().filter(m -> m.getTipo() == TipoMovimento.DEBITO).count();
                long total = movs.size();

                BigDecimal valorCredito = movs.stream()
                                .filter(m -> m.getTipo() == TipoMovimento.CREDITO)
                                .map(Movimento::getValor)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal valorDebito = movs.stream()
                                .filter(m -> m.getTipo() == TipoMovimento.DEBITO)
                                .map(Movimento::getValor)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal saldoInicial = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                BigDecimal saldoAtual = saldoInicial.add(valorCredito).subtract(valorDebito)
                                .setScale(2, RoundingMode.HALF_UP);

                BigDecimal valorPago = calcularTaxaPorPeriodosDe30Dias(cliente, movs);

                return new RelatorioSaldoClienteDTO(
                                cliente.getNome(),
                                java.time.LocalDateTime
                                                .ofInstant(cliente.getCreatedAt(), java.time.ZoneId.systemDefault())
                                                .toLocalDate(),
                                enderecoFmt,
                                qtdCred,
                                qtdDeb,
                                total,
                                valorPago,
                                saldoInicial,
                                saldoAtual);
        }

        // Regra: a cada janela de 30 dias desde o createdAt do cliente
        // 1..10 movs => R$1,00 por movimentação
        // 11..20 movs => R$0,75 por movimentação
        // >20 movs => R$0,50 por movimentação
        private BigDecimal calcularTaxaPorPeriodosDe30Dias(Cliente cliente, List<Movimento> movs) {
                if (movs.isEmpty())
                        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

                var base = cliente.getCreatedAt();
                var baseOffset = java.time.OffsetDateTime.ofInstant(base, ZoneId.systemDefault());
                Map<Long, Long> contagemPorJanela = movs.stream()
                                .collect(Collectors.groupingBy(m -> (Long) janelaIndex(baseOffset, m),
                                                Collectors.counting()));

                BigDecimal total = BigDecimal.ZERO;
                for (var entry : contagemPorJanela.entrySet()) {
                        long q = entry.getValue();
                        BigDecimal precoUnit = q <= 10 ? bd("1.00") : (q <= 20 ? bd("0.75") : bd("0.50"));
                        total = total.add(precoUnit.multiply(BigDecimal.valueOf(q)));
                }
                return total.setScale(2, RoundingMode.HALF_UP);
        }

        private long janelaIndex(java.time.OffsetDateTime base, Movimento m) {
                var d1 = base.atZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
                var d2 = m.getDataHora().atZone(ZoneId.systemDefault()).toLocalDate();
                long days = java.time.temporal.ChronoUnit.DAYS.between(d1, d2);
                return Math.max(0, days / 30);
        }

        private static String nullToEmpty(String s) {
                return s == null ? "" : s;
        }

        // package: br.com.desafio.celula_financeiro_controladoria.domain.service
        @Transactional(readOnly = true)
        public RelatorioSaldoClientePeriodoDTO saldoClientePeriodo(Long clienteId, LocalDate ini, LocalDate fim) {
                Cliente cliente = clienteRepo.findById(clienteId)
                                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

                Endereco end = enderecoRepo.findByClienteId(clienteId).orElse(null);
                String enderecoFmt = end == null ? "—"
                                : String.format("%s, %s%s, %s, %s, %s, %s",
                                                nz(end.getLogradouro()),
                                                nz(end.getNumero()),
                                                (end.getComplemento() == null || end.getComplemento().isBlank()) ? ""
                                                                : " - " + end.getComplemento(),
                                                nz(end.getBairro()),
                                                nz(end.getCidade()),
                                                nz(end.getUf()),
                                                nz(end.getCep()));

                var inicioDT = ini.atStartOfDay();
                var fimDT = fim.atTime(23, 59, 59);

                List<Movimento> movs = movimentoRepo.findByClienteIdAndPeriodo(clienteId, inicioDT, fimDT);

                long qtdCred = movs.stream().filter(m -> m.getTipo() == TipoMovimento.CREDITO).count();
                long qtdDeb = movs.stream().filter(m -> m.getTipo() == TipoMovimento.DEBITO).count();
                long total = movs.size();

                BigDecimal valorCredito = movs.stream()
                                .filter(m -> m.getTipo() == TipoMovimento.CREDITO)
                                .map(Movimento::getValor)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal valorDebito = movs.stream()
                                .filter(m -> m.getTipo() == TipoMovimento.DEBITO)
                                .map(Movimento::getValor)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // seguimos o enunciado (exemplo mostra saldo inicial 0,00 no relatório de
                // período)
                BigDecimal saldoInicial = bd("0.00");
                BigDecimal saldoAtual = saldoInicial.add(valorCredito).subtract(valorDebito);

                // taxa por janelas de 30 dias (partindo do createdAt do cliente),
                // porém contando só as movimentações que caíram no período informado
                BigDecimal valorPago = calcularTaxaPorPeriodosDe30Dias(cliente, movs);

                return new RelatorioSaldoClientePeriodoDTO(
                                ini,
                                fim,
                                cliente.getNome(),
                                java.time.LocalDateTime
                                                .ofInstant(cliente.getCreatedAt(), java.time.ZoneId.systemDefault())
                                                .toLocalDate(),
                                enderecoFmt,
                                qtdCred,
                                qtdDeb,
                                total,
                                valorPago,
                                saldoInicial,
                                saldoAtual);
        }

        private static String nz(String s) {
                return s == null ? "" : s;
        }

        private static BigDecimal bd(String v) {
                return new BigDecimal(v).setScale(2);
        }

}
