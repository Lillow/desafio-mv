package br.com.desafio.celula_financeiro_controladoria.relat_test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class RelatSaldCliTest {

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
}
