package br.com.desafio.celula_financeiro_controladoria.cliente;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.desafio.celula_financeiro_controladoria.domain.controller.ClienteController;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.Cliente;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.ClientePF;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.ClientePJ;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class ClienteTest {

    @Autowired
    private ClienteController clienteController;

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

    private ClientePJ novoPJ(String nomeFantasia, String cnpj, String razao) {
        ClientePJ pj = new ClientePJ();
        pj.setNome(nomeFantasia);
        pj.setEmail(nomeFantasia.toLowerCase().replace(" ", ".") + "@corp.com");
        pj.setTelefone("1133334444");
        pj.setCnpj(cnpj);
        pj.setRazaoSocial(razao);
        pj.setInscEstadual("IS-123");
        pj.setAtivo(true);
        return pj;
    }

    // ---------- testes ----------

    @Test
    void deveCriarEBuscarClientePF() {
        ResponseEntity<ClientePF> respCriacao = clienteController.criarPF(novoPF("João da Silva", "12345678901"));
        assertThat(respCriacao.getStatusCode().is2xxSuccessful()).isTrue();

        ClientePF criado = respCriacao.getBody();
        assertThat(criado).isNotNull();
        assertThat(criado.getId()).isNotNull();
        assertThat(criado.getCpf()).isEqualTo("12345678901");

        ResponseEntity<Cliente> respBusca = clienteController.buscar(criado.getId());
        assertThat(respBusca.getStatusCode().is2xxSuccessful()).isTrue();

        Cliente buscado = respBusca.getBody();
        assertThat(buscado).isNotNull();
        assertThat(buscado.getNome()).isEqualTo("João da Silva");
    }

    @Test
    void deveCriarEListarClientes() {
        clienteController.criarPF(novoPF("PF A", "11111111111"));
        clienteController.criarPJ(novoPJ("PJ A", "11222333000199", "PJ A LTDA"));

        ResponseEntity<List<Cliente>> lista = clienteController.listar();
        assertThat(lista.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(lista.getBody()).isNotNull();
        assertThat(lista.getBody().size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void deveAtualizarPF_SemPermitirAlterarCPF() {
        ClientePF pf = clienteController.criarPF(novoPF("Maria", "98765432100")).getBody();
        assertThat(pf).isNotNull();
        Long id = pf.getId();

        // tentativa de alterar apenas nome/email (ok)
        ClientePF reqOk = new ClientePF();
        reqOk.setNome("Maria Atualizada");
        reqOk.setEmail("maria@newmail.com");
        reqOk.setTelefone("11988887777");
        reqOk.setDataNasc(LocalDate.of(1991, 5, 20));
        reqOk.setRg("7654321");
        reqOk.setCpf("98765432100"); // igual ao original

        ClientePF atualizado = clienteController.atualizarPF(id, reqOk).getBody();
        assertThat(atualizado).isNotNull();
        assertThat(atualizado.getNome()).isEqualTo("Maria Atualizada");
        assertThat(atualizado.getCpf()).isEqualTo("98765432100");

        // tentativa de trocar CPF (deve lançar IllegalArgumentException do service)
        ClientePF reqInvalido = new ClientePF();
        reqInvalido.setNome("Maria 2");
        reqInvalido.setCpf("00000000000"); // diferente

        assertThatThrownBy(() -> clienteController.atualizarPF(id, reqInvalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CPF não pode ser alterado");
    }

    @Test
    void deveAtualizarPJ_SemPermitirAlterarCNPJ() {
        ClientePJ pj = clienteController.criarPJ(novoPJ("Loja XPTO", "22333444000166", "Loja XPTO LTDA")).getBody();
        assertThat(pj).isNotNull();
        Long id = pj.getId();

        ClientePJ reqOk = new ClientePJ();
        reqOk.setNome("Loja XPTO Matriz");
        reqOk.setEmail("contato@xpto.com");
        reqOk.setTelefone("1130303030");
        reqOk.setRazaoSocial("Loja XPTO LTDA"); // pode alterar
        reqOk.setInscEstadual("IS-999");
        reqOk.setCnpj("22333444000166"); // igual ao original

        ClientePJ atualizado = clienteController.atualizarPJ(id, reqOk).getBody();
        assertThat(atualizado).isNotNull();
        assertThat(atualizado.getNome()).isEqualTo("Loja XPTO Matriz");
        assertThat(atualizado.getCnpj()).isEqualTo("22333444000166");

        ClientePJ reqInvalido = new ClientePJ();
        reqInvalido.setCnpj("00011122000100"); // diferente

        assertThatThrownBy(() -> clienteController.atualizarPJ(id, reqInvalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CNPJ não pode ser alterado");
    }

    @Test
    void deveInativarCliente() {
        ClientePF pf = clienteController.criarPF(novoPF("Carlos", "55544433322")).getBody();
        assertThat(pf).isNotNull();

        // inativa
        var resp = clienteController.inativar(pf.getId());
        assertThat(resp.getStatusCode().is2xxSuccessful() || resp.getStatusCode().is2xxSuccessful()).isTrue();
        // confirma
        Cliente apos = clienteController.buscar(pf.getId()).getBody();
        assertThat(apos).isNotNull();
        assertThat(apos.getAtivo()).isFalse();
    }
}
