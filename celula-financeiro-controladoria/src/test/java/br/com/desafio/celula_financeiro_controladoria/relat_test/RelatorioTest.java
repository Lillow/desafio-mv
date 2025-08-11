package br.com.desafio.celula_financeiro_controladoria.relat_test;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import br.com.desafio.celula_financeiro_controladoria.domain.controller.ClienteController;
import br.com.desafio.celula_financeiro_controladoria.domain.controller.ContaController;
import br.com.desafio.celula_financeiro_controladoria.domain.controller.EnderecoController;
import br.com.desafio.celula_financeiro_controladoria.domain.controller.RelatorioController;
import br.com.desafio.celula_financeiro_controladoria.domain.dto.EnderecoDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.dto.RelatorioSaldoClienteDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.Conta;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.Movimento;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.ClientePF;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.StatusConta;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoConta;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoMovimento;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.MovimentoRepository;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class RelatorioTest {

    @Autowired
    private ClienteController clienteController;
    @Autowired
    private EnderecoController enderecoController;
    @Autowired
    private ContaController contaController;
    @Autowired
    private RelatorioController relatorioController;

    // usamos repo de movimento só para montar o cenário rapidamente
    @Autowired
    private MovimentoRepository movimentoRepo;

    @Test
    void deveGerarRelatorioDeSaldoDoCliente() {
        // 1) cria cliente PF
        ClientePF pf = new ClientePF();
        pf.setNome("Cliente X");
        pf.setEmail("x@mail.com");
        pf.setTelefone("11999999999");
        pf.setCpf("10020030040");
        pf = clienteController.criarPF(pf).getBody();
        assertThat(pf).isNotNull();

        // 2) cria endereço
        EnderecoDTO e = new EnderecoDTO();
        e.setLogradouro("Rua A");
        e.setNumero("123");
        e.setComplemento("Ap 10");
        e.setBairro("Centro");
        e.setCidade("São Paulo");
        e.setUf("SP");
        e.setCep("01001000");
        e.setAtivo(true);
        enderecoController.criar(pf.getId(), e);

        // 3) cria conta
        Conta c = new Conta();
        c.setAgencia("0001");
        c.setNumero("12345-6");
        c.setDocumento("10020030040");
        c.setTipoConta(TipoConta.CORRENTE);
        c.setStatus(StatusConta.ATIVA);
        c = contaController.criar(pf.getId(), c).getBody();
        assertThat(c).isNotNull();

        // 4) cria movimentações (mesma janela de 30 dias => valor por operação = 1.00,
        // pois 4 movs)
        movimentoRepo.saveAll(List.of(
                mov(c, TipoMovimento.CREDITO, bd("100.00")),
                mov(c, TipoMovimento.DEBITO, bd("30.00")),
                mov(c, TipoMovimento.CREDITO, bd("50.00")),
                mov(c, TipoMovimento.DEBITO, bd("10.00"))));

        // 5) gera relatório
        RelatorioSaldoClienteDTO dto = relatorioController
                .relatorioSaldo(pf.getId())
                .getBody();

        assertThat(dto).isNotNull();
        assertThat(dto.cliente()).isEqualTo("Cliente X");
        assertThat(dto.endereco()).contains("Rua A, 123", "Centro", "São Paulo", "SP", "01001000");

        // contagens
        assertThat(dto.movsCredito()).isEqualTo(2);
        assertThat(dto.movsDebito()).isEqualTo(2);
        assertThat(dto.totalMovs()).isEqualTo(4);

        // taxas: 4 movimentos na janela => 4 * 1.00
        assertThat(dto.valorPagoMovimentacoes()).isEqualByComparingTo(bd("4.00"));

        // saldos
        // crédito total = 150.00, débito total = 40.00 => atual = 110.00 (inicial 0.00)
        assertThat(dto.saldoInicial()).isEqualByComparingTo(bd("0.00"));
        assertThat(dto.saldoAtual()).isEqualByComparingTo(bd("110.00"));
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    private Movimento mov(Conta c, TipoMovimento tipo, BigDecimal valor) {
        Movimento m = new Movimento();
        m.setConta(c);
        m.setTipo(tipo);
        m.setValor(valor);
        m.setOrigem("IF");
        m.setDescricao("teste");
        m.setDataHora(LocalDateTime.now());
        m.setAtivo(true);
        return m;
    }

    @Test
    void deveGerarRelatorioDeSaldoDoClientePorPeriodo() {
        // cria cliente
        ClientePF pf = new ClientePF();
        pf.setNome("Cliente Periodo");
        pf.setEmail("periodo@mail.com");
        pf.setTelefone("11999990000");
        pf.setCpf("11122233344");
        pf = clienteController.criarPF(pf).getBody();
        assertThat(pf).isNotNull();

        // endereço
        EnderecoDTO e = new EnderecoDTO();
        e.setLogradouro("Rua P");
        e.setNumero("10");
        e.setBairro("Centro");
        e.setCidade("SP");
        e.setUf("SP");
        e.setCep("01001000");
        e.setAtivo(true);
        enderecoController.criar(pf.getId(), e);

        // conta
        Conta c = new Conta();
        c.setAgencia("0002");
        c.setNumero("9999-9");
        c.setDocumento("11122233344");
        c.setTipoConta(TipoConta.CORRENTE);
        c.setStatus(StatusConta.ATIVA);
        c = contaController.criar(pf.getId(), c).getBody();
        assertThat(c).isNotNull();

        // período alvo
        var from = LocalDate.now().minusDays(5);
        var to = LocalDate.now().plusDays(5);

        // movimentações dentro do período
        movimentoRepo.saveAll(List.of(
                mov(c, TipoMovimento.CREDITO, bd("100.00"), LocalDateTime.now().minusDays(1)),
                mov(c, TipoMovimento.DEBITO, bd("30.00"), LocalDateTime.now()),
                mov(c, TipoMovimento.CREDITO, bd("50.00"), LocalDateTime.now().plusDays(1))));
        // fora do período (não deve contar)
        movimentoRepo.save(mov(c, TipoMovimento.DEBITO, bd("999.99"), LocalDateTime.now().minusDays(40)));

        var dto = relatorioController
                .relatorioSaldoPeriodo(pf.getId(), from, to)
                .getBody();

        assertThat(dto).isNotNull();
        assertThat(dto.periodoInicio()).isEqualTo(from);
        assertThat(dto.periodoFim()).isEqualTo(to);
        assertThat(dto.cliente()).isEqualTo("Cliente Periodo");
        assertThat(dto.movsCredito()).isEqualTo(2);
        assertThat(dto.movsDebito()).isEqualTo(1);
        assertThat(dto.totalMovs()).isEqualTo(3);

        // taxa: 3 movimentos na mesma janela => 3 * 1.00
        assertThat(dto.valorPagoMovimentacoes()).isEqualByComparingTo(bd("3.00"));

        // saldo período: (100 + 50) - 30 = 120.00, com saldo inicial 0.00
        assertThat(dto.saldoInicial()).isEqualByComparingTo(bd("0.00"));
        assertThat(dto.saldoAtual()).isEqualByComparingTo(bd("120.00"));
    }

    private Movimento mov(Conta c, TipoMovimento tipo, BigDecimal valor, LocalDateTime data) {
        Movimento m = new Movimento();
        m.setConta(c);
        m.setTipo(tipo);
        m.setValor(valor);
        m.setOrigem("IF");
        m.setDescricao("teste");
        m.setDataHora(data);
        m.setAtivo(true);
        return m;
    }

    @Test
    void deveGerarRelatorioDeSaldoDeTodosClientesNaData() {
        // clientes
        var pfA = new ClientePF();
        pfA.setNome("X");
        pfA.setEmail("x@mail.com");
        pfA.setTelefone("1");
        pfA.setCpf("10020030001");
        pfA = clienteController.criarPF(pfA).getBody();

        var pfB = new ClientePF();
        pfB.setNome("Y");
        pfB.setEmail("y@mail.com");
        pfB.setTelefone("2");
        pfB.setCpf("10020030002");
        pfB = clienteController.criarPF(pfB).getBody();

        assertThat(pfA).isNotNull();
        assertThat(pfB).isNotNull();

        // contas
        var cA = new Conta();
        cA.setAgencia("0001");
        cA.setNumero("1");
        cA.setDocumento("10020030001");
        cA.setTipoConta(TipoConta.CORRENTE);
        cA.setStatus(StatusConta.ATIVA);
        cA = contaController.criar(pfA.getId(), cA).getBody();

        var cB = new Conta();
        cB.setAgencia("0001");
        cB.setNumero("2");
        cB.setDocumento("10020030002");
        cB.setTipoConta(TipoConta.CORRENTE);
        cB.setStatus(StatusConta.ATIVA);
        cB = contaController.criar(pfB.getId(), cB).getBody();

        // movimentos
        var alvo = LocalDate.now();
        movimentoRepo.saveAll(List.of(
                mov(cA, TipoMovimento.CREDITO, bd("200.00"), alvo.atTime(10, 0)),
                mov(cA, TipoMovimento.DEBITO, bd("50.00"), alvo.atTime(11, 0)),
                mov(cB, TipoMovimento.CREDITO, bd("80.00"), alvo.minusDays(1).atTime(10, 0)),
                mov(cB, TipoMovimento.DEBITO, bd("20.00"), alvo.atTime(9, 0)),
                // fora da data (não conta para saldo na data):
                mov(cB, TipoMovimento.DEBITO, bd("999.00"), alvo.plusDays(1).atTime(10, 0))));

        var lista = relatorioController.saldosClientesEm(alvo).getBody();
        assertThat(lista).isNotNull();
        // saldo X = 200 - 50 = 150
        assertThat(lista.stream().filter(l -> l.cliente().equals("X")).findFirst().get().saldo())
                .isEqualByComparingTo(bd("150.00"));
        // saldo Y = 80 - 20 = 60
        assertThat(lista.stream().filter(l -> l.cliente().equals("Y")).findFirst().get().saldo())
                .isEqualByComparingTo(bd("60.00"));
    }

    @Test
    void deveGerarRelatorioDeReceitaDaEmpresaPorPeriodo() {
        // C1 com 8 movs na janela -> 8 * 1.00
        var c1 = novoClienteComConta("Cliente A", "11111111111");
        var conta1 = c1.conta();

        // C2 com 15 movs na janela -> 15 * 0.75
        var c2 = novoClienteComConta("Cliente B", "22222222222");
        var conta2 = c2.conta();

        var from = LocalDate.now().minusDays(2);
        var to = LocalDate.now().plusDays(2);

        // cria 8 movs pro C1
        for (int i = 0; i < 8; i++) {
            movimentoRepo.save(mov(conta1, TipoMovimento.CREDITO, bd("10.00"), LocalDateTime.now().minusDays(1)));
        }

        // cria 15 movs pro C2
        for (int i = 0; i < 15; i++) {
            movimentoRepo.save(mov(conta2, (i % 2 == 0 ? TipoMovimento.CREDITO : TipoMovimento.DEBITO), bd("5.00"),
                    LocalDateTime.now()));
        }

        // um movimento fora do período para C2 (não conta)
        movimentoRepo.save(mov(conta2, TipoMovimento.CREDITO, bd("999.00"), LocalDateTime.now().minusDays(40)));

        var dto = relatorioController.receita(from, to).getBody();
        assertThat(dto).isNotNull();
        assertThat(dto.inicio()).isEqualTo(from);
        assertThat(dto.fim()).isEqualTo(to);

        var linhaA = dto.clientes().stream().filter(l -> l.cliente().equals("Cliente A")).findFirst().get();
        var linhaB = dto.clientes().stream().filter(l -> l.cliente().equals("Cliente B")).findFirst().get();
        var linhaC = dto.clientes().stream().filter(l -> l.cliente().equals("Cliente C")).findFirst().get();

        assertThat(linhaA.quantidadeMovs()).isEqualTo(8);
        assertThat(linhaA.valorCobrado()).isEqualByComparingTo(bd("8.00"));

        assertThat(linhaB.quantidadeMovs()).isEqualTo(15);
        // 15 movimentos na mesma janela: 15 * 0.75 = 11.25
        assertThat(linhaB.valorCobrado()).isEqualByComparingTo(bd("11.25"));

        assertThat(linhaC.quantidadeMovs()).isEqualTo(0);
        assertThat(linhaC.valorCobrado()).isEqualByComparingTo(bd("0.00"));

        assertThat(dto.total()).isEqualByComparingTo(bd("19.25"));
    }

    // ---- helpers específicos deste teste ----
    private record CliConta(ClientePF cliente, Conta conta) {
    }

    private CliConta novoClienteComConta(String nome, String cpf) {
        var pf = new ClientePF();
        pf.setNome(nome);
        pf.setEmail(nome.toLowerCase().replace(" ", ".") + "@mail.com");
        pf.setTelefone("1");
        pf.setCpf(cpf);
        pf = clienteController.criarPF(pf).getBody();

        var c = new Conta();
        c.setAgencia("0001");
        c.setNumero(UUID.randomUUID().toString().substring(0, 6));
        c.setDocumento(cpf);
        c.setTipoConta(TipoConta.CORRENTE);
        c.setStatus(StatusConta.ATIVA);
        c = contaController.criar(pf.getId(), c).getBody();

        return new CliConta(pf, c);
    }

}
