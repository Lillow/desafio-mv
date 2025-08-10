package br.com.desafio.celula_financeiro_controladoria.domain.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.ReceitaClienteDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.dto.RelatorioSaldoDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.Endereco;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.Movimento;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.Cliente;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoMovimento;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.MovimentoRepository;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.cliente.ClienteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final MovimentoRepository movRepo;
    private final ClienteRepository clienteRepo; // supondo generico
    private final TarifaService tarifaService;

    // período de 30 dias a partir da data de cadastro (para cobrança “mensal” por
    // cliente)
    public RelatorioSaldoDTO saldoCliente(Long clienteId, LocalDate ini, LocalDate fim) {
        Cliente cliente = clienteRepo.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        LocalDateTime dtIni = (ini != null ? ini
                : LocalDate.ofInstant(cliente.getCreatedAt(), java.time.ZoneId.systemDefault())).atStartOfDay();
        LocalDateTime dtFim = (fim != null ? fim : LocalDate.now()).atTime(23, 59, 59);

        var movs = movRepo.findByClienteAndPeriodo(clienteId, dtIni, dtFim);

        long qtdCred = movs.stream().filter(m -> m.getTipo() == TipoMovimento.CREDITO).count();
        long qtdDeb = movs.stream().filter(m -> m.getTipo() == TipoMovimento.DEBITO).count();
        long total = movs.size();

        // saldo inicial = saldo no início do período (simplificação: recalcular a
        // partir de 0, ou se vc guarda snapshot, use-o)
        // aqui, simples: pega saldo atual do somatório até dtIni exclusive
        BigDecimal saldoInicial = calcularSaldoAte(clienteId, dtIni);
        BigDecimal saldoAtual = saldoInicial
                .add(movs.stream().filter(m -> m.getTipo() == TipoMovimento.CREDITO)
                        .map(Movimento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add))
                .subtract(movs.stream().filter(m -> m.getTipo() == TipoMovimento.DEBITO)
                        .map(Movimento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add));

        BigDecimal valorPago = tarifaService.calcularValorTotal(total);

        RelatorioSaldoDTO dto = new RelatorioSaldoDTO();
        dto.setClienteId(cliente.getId());
        dto.setClienteNome(cliente.getNome());
        dto.setClienteDesde(
                LocalDateTime.ofInstant(cliente.getCreatedAt(), java.time.ZoneId.systemDefault()).toLocalDate());
        dto.setEndereco(Endereco.from(cliente.getEndereco()));
        dto.setQtdCredito(qtdCred);
        dto.setQtdDebito(qtdDeb);
        dto.setTotalMovs(total);
        dto.setValorPagoMovimentacoes(valorPago);
        dto.setSaldoInicial(saldoInicial);
        dto.setSaldoAtual(saldoAtual);
        dto.setDataRefInicio(dtIni.toLocalDate());
        dto.setDataRefFim(dtFim.toLocalDate());
        return dto;
    }

    public List<ReceitaClienteDTO> receitaPorPeriodo(LocalDate ini, LocalDate fim) {
        LocalDateTime dtIni = ini.atStartOfDay();
        LocalDateTime dtFim = fim.atTime(23, 59, 59);
        // busque todos os clientes e monte agregados (para performance, dá pra fazer
        // query nativa agrupando)
        return clienteRepo.findAll().stream().map(c -> {
            long qtd = movRepo.countByClienteNoPeriodo(c.getId(), dtIni, dtFim);
            BigDecimal valor = tarifaService.calcularValorTotal(qtd);
            var r = new ReceitaClienteDTO();
            r.setClienteId(c.getId());
            r.setClienteNome(c.getNome());
            r.setQuantidadeMovs(qtd);
            r.setValorTarifado(valor);
            return r;
        }).toList();
    }

    private BigDecimal calcularSaldoAte(Long clienteId, LocalDateTime ate) {
        // simplificação: carrega movimentos do cliente até ate e aplica
        var movs = movRepo.findByClienteAndPeriodo(clienteId, LocalDateTime.MIN, ate.minusSeconds(1));
        BigDecimal saldo = BigDecimal.ZERO;
        for (var m : movs) {
            saldo = (m.getTipo() == TipoMovimento.CREDITO) ? saldo.add(m.getValor()) : saldo.subtract(m.getValor());
        }
        return saldo.max(BigDecimal.ZERO); // se quiser não negativo
    }
}
