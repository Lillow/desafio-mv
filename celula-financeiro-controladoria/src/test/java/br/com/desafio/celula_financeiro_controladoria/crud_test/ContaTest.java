package br.com.desafio.celula_financeiro_controladoria.crud_test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.desafio.celula_financeiro_controladoria.domain.controller.ClienteController;
import br.com.desafio.celula_financeiro_controladoria.domain.controller.ContaController;
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
class ContaTest {

    @Autowired
    private ContaController contaController;

    @Autowired
    private ClienteController clienteController;

    // usaremos o repo só para "simular" que já existem movimentações
    @Autowired
    private MovimentoRepository movimentoRepo;

    // ---------- helpers ----------

    private ClientePF novoPF(String nome, String cpf) {
        ClientePF pf = new ClientePF();
        pf.setNome(nome);
        pf.setEmail(nome.toLowerCase().replace(" ", ".") + "@mail.com");
        pf.setTelefone("11999999999");
        pf.setCpf(cpf);
        pf.setDataNasc(LocalDate.of(1990, 1, 1));
        pf.setRg("1234567");
        pf.setAtivo(true);
        return pf;
    }

    private Long criarClientePF(String nome, String cpf) {
        ClientePF salvo = clienteController.criarPF(novoPF(nome, cpf)).getBody();
        assertThat(salvo).isNotNull();
        return salvo.getId();
    }

    private Conta novaConta(String agencia, String numero, String documento, TipoConta tipo) {
        Conta c = new Conta();
        c.setAgencia(agencia);
        c.setNumero(numero);
        c.setDocumento(documento);
        c.setTipoConta(tipo);
        c.setStatus(StatusConta.ATIVA);
        c.setSaldo(new BigDecimal("0.00"));
        c.setAtivo(true);
        return c;
    }

    private Conta criarContaParaCliente(Long clienteId, String agencia, String numero) {
        Conta req = novaConta(agencia, numero, "12345678901", TipoConta.CORRENTE);
        ResponseEntity<Conta> resp = contaController.criar(clienteId, req);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        return resp.getBody();
    }

    private void inserirMovimento(Conta conta, TipoMovimento tipo, String valor) {
        Movimento m = new Movimento();
        m.setConta(conta);
        m.setTipo(tipo);
        m.setValor(new BigDecimal(valor));
        m.setDescricao("teste");
        m.setOrigem("INTEG");
        m.setDataHora(LocalDateTime.now());
        m.setAtivo(true);
        movimentoRepo.saveAndFlush(m);
    }

    // ---------- testes ----------

    @Test
    void deveCriarEListarContasPorCliente() {
        Long clienteId = criarClientePF("João", "12345678901");

        criarContaParaCliente(clienteId, "0001", "12345-6");
        criarContaParaCliente(clienteId, "0001", "12345-7");

        ResponseEntity<List<Conta>> lista = contaController.listarPorCliente(clienteId);
        assertThat(lista.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(lista.getBody()).isNotNull();
        assertThat(lista.getBody().size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void deveAtualizarConta_QuandoNaoTemMovimento() {
        Long clienteId = criarClientePF("Maria", "98765432100");
        Conta conta = criarContaParaCliente(clienteId, "0002", "22222-2");

        Conta req = new Conta();
        req.setAgencia("0003");
        req.setNumero("33333-3");
        req.setDocumento("00011122233");
        req.setTipoConta(TipoConta.POUPANCA);
        req.setStatus(StatusConta.BLOQUEADA);

        Conta atualizado = contaController.atualizar(conta.getId(), req).getBody();
        assertThat(atualizado).isNotNull();
        assertThat(atualizado.getAgencia()).isEqualTo("0003");
        assertThat(atualizado.getNumero()).isEqualTo("33333-3");
        assertThat(atualizado.getDocumento()).isEqualTo("00011122233");
        assertThat(atualizado.getTipoConta()).isEqualTo(TipoConta.POUPANCA);
        assertThat(atualizado.getStatus()).isEqualTo(StatusConta.BLOQUEADA);
    }

    @Test
    void naoDeveAtualizarConta_QuandoTemMovimento() {
        Long clienteId = criarClientePF("Carlos", "55544433322");
        Conta conta = criarContaParaCliente(clienteId, "0004", "44444-4");

        // simula que já existem movimentos vinculados à conta
        inserirMovimento(conta, TipoMovimento.CREDITO, "100.00");

        Conta req = new Conta();
        req.setNumero("99999-9"); // tentativa de alteração

        assertThatThrownBy(() -> contaController.atualizar(conta.getId(), req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("movimenta"); // ex: "possui movimentação e não pode ser alterada"
    }

    @Test
    void deveExcluirLogicamenteConta() {
        Long clienteId = criarClientePF("Ana", "10101010101");
        Conta conta = criarContaParaCliente(clienteId, "0005", "55555-5");

        var resp = contaController.remover(conta.getId());
        assertThat(resp.getStatusCode().is2xxSuccessful() || resp.getStatusCode().value() == 204).isTrue();

        Conta apos = contaController.buscarPorId(conta.getId()).getBody();
        assertThat(apos).isNotNull();
        assertThat(apos.getAtivo()).isFalse();
    }

    @Test
    void naoDevePermitirDuplicidadeAgenciaNumero() {
        Long clienteId = criarClientePF("Bruno", "20202020202");
        criarContaParaCliente(clienteId, "0099", "123-A");

        // mesma (agencia, numero) — a constraint é global (não por cliente)
        Conta duplicada = novaConta("0099", "123-A", "99988877766", TipoConta.CORRENTE);

        assertThatThrownBy(() -> contaController.criar(clienteId, duplicada))
                .isInstanceOfAny(IllegalStateException.class, DataIntegrityViolationException.class);
    }

    @Test
    void removerContaComMovimento_deveSerLogico_semErro() {
        Long clienteId = criarClientePF("Diego", "30303030303");
        Conta conta = criarContaParaCliente(clienteId, "0010", "10101-0");

        // cria um movimento
        inserirMovimento(conta, TipoMovimento.DEBITO, "50.00");

        // remover deve ser lógico e não lançar erro
        var resp = contaController.remover(conta.getId());
        assertThat(resp.getStatusCode().is2xxSuccessful() || resp.getStatusCode().value() == 204).isTrue();

        Conta apos = contaController.buscarPorId(conta.getId()).getBody();
        assertThat(apos).isNotNull();
        assertThat(apos.getAtivo()).isFalse();
    }
}
