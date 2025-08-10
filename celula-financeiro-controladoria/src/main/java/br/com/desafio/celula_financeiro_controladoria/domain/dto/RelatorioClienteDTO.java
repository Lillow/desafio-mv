package br.com.desafio.celula_financeiro_controladoria.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.time.ZoneId;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.Endereco;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.Cliente;

public record RelatorioClienteDTO(
                Long clienteId,
                String cliente,
                LocalDate clienteDesde,
                String enderecoFormatado,
                long movCredito,
                long movDebito,
                long movTotal,
                BigDecimal valorPagoMov,
                BigDecimal saldoInicial,
                BigDecimal saldoAtual,
                OffsetDateTime periodoIni,
                OffsetDateTime periodoFim) {
        public static RelatorioClienteDTO from(Cliente c,
                        BigDecimal creditos,
                        BigDecimal debitos,
                        long qtd,
                        BigDecimal valorPago,
                        BigDecimal saldoInicial,
                        BigDecimal saldoAtual,
                        OffsetDateTime ini,
                        OffsetDateTime fim) {
                Endereco end = c.getEndereco();
                String endFmt = end == null ? ""
                                : String.format("%s, %s, %s, %s, %s, %s, %s",
                                                end.getLogradouro(), end.getNumero(),
                                                n(end.getComplemento()), n(end.getBairro()),
                                                n(end.getCidade()), n(end.getUf()),
                                                n(end.getCep()));
                long qCred = 0; // se quiser separar contagens por tipo, ajuste o repo
                long qDeb = 0; // ajuste conforme necessário para separar débitos

                return new RelatorioClienteDTO(
                                c.getId(),
                                c.getNome(),
                                LocalDateTime.ofInstant(c.getCreatedAt(), ZoneId.systemDefault()).toLocalDate(),
                                endFmt,
                                qCred,
                                qDeb,
                                qtd,
                                valorPago,
                                saldoInicial,
                                saldoAtual,
                                ini,
                                fim);
        }

        private static String n(String v) {
                return v == null ? "" : v;
        }

        public record ResumoSaldoClienteDTO(Long clienteId, String cliente, LocalDate clienteDesde,
                        BigDecimal saldoNaData) {
        }
}
