package br.com.desafio.celula_financeiro_controladoria.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.ClienteSaldoNaDataDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.dto.ReceitaClientePeriodoDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.dto.ReceitaEmpresaPeriodoDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.dto.RelatorioSaldoClienteDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.dto.RelatorioSaldoClientePeriodoDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.Endereco;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.Movimento;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.Cliente;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoMovimento;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.EnderecoRepository;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.MovimentoRepository;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.cliente.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelatorioService {

        private static final RoundingMode ROUND = RoundingMode.HALF_UP;
        private static final BigDecimal BD_ZERO = BigDecimal.ZERO.setScale(2, ROUND);

        @Autowired
        private ClienteRepository clienteRepo;

        @Autowired
        private EnderecoRepository enderecoRepo;

        @Autowired
        private MovimentoRepository movimentoRepo;

        public RelatorioSaldoClienteDTO saldoCliente(Long clienteId) {
                Cliente cliente = clienteRepo.findById(clienteId)
                                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

                Endereco end = enderecoRepo.findByClienteId(clienteId).orElse(null);
                String enderecoFmt = fmtEndereco(end);

                List<Movimento> movs = movimentoRepo.findByClienteId(clienteId);

                long qtdCred = movs.stream().filter(m -> m.getTipo() == TipoMovimento.CREDITO).count();
                long qtdDeb = movs.stream().filter(m -> m.getTipo() == TipoMovimento.DEBITO).count();

                BigDecimal valorCredito = soma(movs, TipoMovimento.CREDITO);
                BigDecimal valorDebito = soma(movs, TipoMovimento.DEBITO);

                BigDecimal saldoInicial = BD_ZERO;
                BigDecimal saldoAtual = saldoInicial.add(valorCredito).subtract(valorDebito).setScale(2, ROUND);

                BigDecimal valorPago = calcularTaxaPorPeriodosDe30Dias(cliente, movs);

                return new RelatorioSaldoClienteDTO(
                                cliente.getNome(),
                                toLocalDate(cliente.getCreatedAt()),
                                enderecoFmt,
                                qtdCred,
                                qtdDeb,
                                movs.size(),
                                valorPago,
                                saldoInicial,
                                saldoAtual);
        }

        // Regra da janela de 30 dias a partir do createdAt do cliente
        private BigDecimal calcularTaxaPorPeriodosDe30Dias(Cliente cliente, List<Movimento> movs) {
                if (movs.isEmpty())
                        return BD_ZERO;

                OffsetDateTime base = cliente.getCreatedAt().atOffset(ZoneOffset.UTC);
                Map<Long, Long> contagemPorJanela = movs.stream()
                                .collect(Collectors.groupingBy(m -> janelaIndex(base, m), Collectors.counting()));

                BigDecimal total = BigDecimal.ZERO;
                for (long q : contagemPorJanela.values()) {
                        BigDecimal unit = q <= 10 ? bd("1.00") : (q <= 20 ? bd("0.75") : bd("0.50"));
                        total = total.add(unit.multiply(BigDecimal.valueOf(q)));
                }
                return total.setScale(2, ROUND);
        }

        private long janelaIndex(OffsetDateTime base, Movimento m) {
                LocalDate d1 = base.atZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
                LocalDate d2 = m.getDataHora().atZone(ZoneId.systemDefault()).toLocalDate();
                long days = ChronoUnit.DAYS.between(d1, d2);
                if (days < 0)
                        return 0; // movimentos antes da base contam na primeira janela
                return days / 30;
        }

        public RelatorioSaldoClientePeriodoDTO saldoClientePeriodo(Long clienteId, LocalDate ini, LocalDate fim) {
                Cliente cliente = clienteRepo.findById(clienteId)
                                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

                Endereco end = enderecoRepo.findByClienteId(clienteId).orElse(null);
                String enderecoFmt = fmtEndereco(end);

                var inicioDT = ini.atStartOfDay();
                var fimDT = fim.atTime(23, 59, 59);

                List<Movimento> movs = movimentoRepo.findByClienteIdAndPeriodo(clienteId, inicioDT, fimDT);

                long qtdCred = movs.stream().filter(m -> m.getTipo() == TipoMovimento.CREDITO).count();
                long qtdDeb = movs.stream().filter(m -> m.getTipo() == TipoMovimento.DEBITO).count();

                BigDecimal valorCredito = soma(movs, TipoMovimento.CREDITO);
                BigDecimal valorDebito = soma(movs, TipoMovimento.DEBITO);

                BigDecimal saldoInicial = BD_ZERO; // conforme enunciado nos exemplos
                BigDecimal saldoAtual = saldoInicial.add(valorCredito).subtract(valorDebito).setScale(2, ROUND);

                BigDecimal valorPago = calcularTaxaPorPeriodosDe30Dias(cliente, movs);

                return new RelatorioSaldoClientePeriodoDTO(
                                ini,
                                fim,
                                cliente.getNome(),
                                toLocalDate(cliente.getCreatedAt()),
                                enderecoFmt,
                                qtdCred,
                                qtdDeb,
                                movs.size(),
                                valorPago,
                                saldoInicial,
                                saldoAtual);
        }

        public List<ClienteSaldoNaDataDTO> saldoTodosClientesNaData(LocalDate data) {
                var fim = data.atTime(23, 59, 59);
                return clienteRepo.findAll().stream().map(cli -> {
                        var movs = movimentoRepo.findByClienteIdAte(cli.getId(), fim);
                        var credito = soma(movs, TipoMovimento.CREDITO);
                        var debito = soma(movs, TipoMovimento.DEBITO);
                        var saldo = credito.subtract(debito).setScale(2, ROUND);
                        return new ClienteSaldoNaDataDTO(
                                        cli.getNome(),
                                        toLocalDate(cli.getCreatedAt()),
                                        data,
                                        saldo);
                }).toList();
        }

        public ReceitaEmpresaPeriodoDTO receitaEmpresaPeriodo(LocalDate inicio, LocalDate fim) {
                var iniDT = inicio.atStartOfDay();
                var fimDT = fim.atTime(23, 59, 59);

                var clientes = clienteRepo.findAll();
                var linhas = new ArrayList<ReceitaClientePeriodoDTO>();
                BigDecimal total = BigDecimal.ZERO;

                for (var cli : clientes) {
                        var movs = movimentoRepo.findByClienteIdAndPeriodo(cli.getId(), iniDT, fimDT);
                        if (movs.isEmpty()) {
                                linhas.add(new ReceitaClientePeriodoDTO(cli.getId(), cli.getNome(), 0L, BD_ZERO));
                                continue;
                        }
                        BigDecimal valorCobrado = calcularTaxaPorPeriodosDe30Dias(cli, movs).setScale(2, ROUND);
                        total = total.add(valorCobrado);
                        linhas.add(new ReceitaClientePeriodoDTO(cli.getId(), cli.getNome(), (long) movs.size(),
                                        valorCobrado));
                }
                return new ReceitaEmpresaPeriodoDTO(inicio, fim, linhas, total.setScale(2, ROUND));
        }

        // ---------- helpers ----------
        private static BigDecimal bd(String v) {
                return new BigDecimal(v).setScale(2, ROUND);
        }

        private static LocalDate toLocalDate(java.time.Instant instant) {
                return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
        }

        private static BigDecimal soma(List<Movimento> movs, TipoMovimento tipo) {
                return movs.stream()
                                .filter(m -> m.getTipo() == tipo)
                                .map(Movimento::getValor)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .setScale(2, ROUND);
        }

        private static String fmtEndereco(Endereco e) {
                if (e == null)
                        return "—";
                List<String> parts = new ArrayList<>();
                addIfNotBlank(parts, e.getLogradouro());
                addIfNotBlank(parts, e.getNumero());
                if (e.getComplemento() != null && !e.getComplemento().isBlank()) {
                        parts.set(parts.size() - 1, parts.get(parts.size() - 1) + " - " + e.getComplemento());
                }
                addIfNotBlank(parts, e.getBairro());
                addIfNotBlank(parts, e.getCidade());
                addIfNotBlank(parts, e.getUf());
                addIfNotBlank(parts, e.getCep());
                return String.join(", ", parts);
        }

        private static void addIfNotBlank(List<String> list, String value) {
                if (value != null && !value.isBlank())
                        list.add(value);
        }
}