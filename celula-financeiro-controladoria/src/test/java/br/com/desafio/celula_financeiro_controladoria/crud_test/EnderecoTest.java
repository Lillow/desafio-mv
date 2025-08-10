package br.com.desafio.celula_financeiro_controladoria.crud_test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import br.com.desafio.celula_financeiro_controladoria.domain.controller.ClienteController;
import br.com.desafio.celula_financeiro_controladoria.domain.controller.EnderecoController;
import br.com.desafio.celula_financeiro_controladoria.domain.dto.EnderecoDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.Endereco;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.ClientePF;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class EnderecoTest {

    @Autowired
    private ClienteController clienteController;

    @Autowired
    private EnderecoController enderecoController;

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

    private Endereco novoEndereco(String cidade) {
        Endereco e = new Endereco();
        e.setLogradouro("Rua A");
        e.setNumero("100");
        e.setComplemento("Ap 10");
        e.setBairro("Centro");
        e.setCidade(cidade);
        e.setUf("SP");
        e.setCep("01001000");
        e.setAtivo(true);
        return e;
    }

    private Long criarClientePF() {
        ResponseEntity<ClientePF> resp = clienteController.criarPF(novoPF("João da Silva", "12345678901"));
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        return resp.getBody().getId();
    }

    // ---------- testes ----------

    @Test
    void deveCriarEBuscarEnderecoPorCliente() {
        Long clienteId = criarClientePF();
        EnderecoDTO enderecoDTO = new EnderecoDTO();
        enderecoDTO.setLogradouro("Rua A");
        enderecoDTO.setNumero("100");
        enderecoDTO.setComplemento("Ap 10");
        enderecoDTO.setBairro("Centro");
        enderecoDTO.setCidade("Recife");
        enderecoDTO.setUf("SP");
        enderecoDTO.setCep("01001000");

        // CREATE
        ResponseEntity<Endereco> criadoResp = enderecoController.criar(clienteId, enderecoDTO);
        assertThat(criadoResp.getStatusCode().is2xxSuccessful()).isTrue();

        Endereco criado = criadoResp.getBody();
        assertThat(criado).isNotNull();
        assertThat(criado.getId()).isNotNull();
        assertThat(criado.getCliente()).isNotNull();
        assertThat(criado.getCliente().getId()).isEqualTo(clienteId);
        assertThat(criado.getCidade()).isEqualTo("Recife");

        // READ by cliente
        ResponseEntity<Endereco> buscadoResp = enderecoController.buscarPorCliente(clienteId);
        assertThat(buscadoResp.getStatusCode().is2xxSuccessful()).isTrue();
        Endereco buscado = buscadoResp.getBody();
        assertThat(buscado).isNotNull();
        assertThat(buscado.getId()).isEqualTo(criado.getId());
    }

    @Test
    void deveAtualizarEndereco_SemAlterarCliente() {
        Long clienteId = criarClientePF();

        EnderecoDTO enderecoDTO = new EnderecoDTO();
        Endereco novoEndereco = novoEndereco("Recife");
        enderecoDTO.setLogradouro(novoEndereco.getLogradouro());
        enderecoDTO.setNumero(novoEndereco.getNumero());
        enderecoDTO.setComplemento(novoEndereco.getComplemento());
        enderecoDTO.setBairro(novoEndereco.getBairro());
        enderecoDTO.setCidade(novoEndereco.getCidade());
        enderecoDTO.setUf(novoEndereco.getUf());
        enderecoDTO.setCep(novoEndereco.getCep());

        Endereco criado = enderecoController.criar(clienteId, enderecoDTO).getBody();
        assertThat(criado).isNotNull();
        Long enderecoId = criado.getId();

        // tenta atualizar dados de endereço (ok) e também "trocar" o cliente (deve
        // ignorar/nao mudar)
        EnderecoDTO reqDTO = new EnderecoDTO();
        reqDTO.setLogradouro("Rua B");
        reqDTO.setNumero("200");
        reqDTO.setComplemento("Casa");
        reqDTO.setBairro("Jardins");
        reqDTO.setCidade("Campinas");
        reqDTO.setUf("SP");
        reqDTO.setCep("13015000");
        // tentativa de trocar cliente (service deve manter o cliente original)
        // ClientePF fakeCliente = new ClientePF();
        // fakeCliente.setId(outroClienteId);
        // reqDTO.setCliente(fakeCliente); // EnderecoDTO does not have setCliente

        Endereco atualizado = enderecoController.atualizar(enderecoId, reqDTO).getBody();
        assertThat(atualizado).isNotNull();
        assertThat(atualizado.getCidade()).isEqualTo("Campinas");
        assertThat(atualizado.getLogradouro()).isEqualTo("Rua B");
        // cliente deve permanecer o original
        assertThat(atualizado.getCliente().getId()).isEqualTo(clienteId);
    }

    @Test
    void naoDevePermitirSegundoEnderecoAtivoParaMesmoCliente() {
        Long clienteId = criarClientePF();

        EnderecoDTO enderecoDTO1 = new EnderecoDTO();
        Endereco endereco1 = novoEndereco("Recife");
        enderecoDTO1.setLogradouro(endereco1.getLogradouro());
        enderecoDTO1.setNumero(endereco1.getNumero());
        enderecoDTO1.setComplemento(endereco1.getComplemento());
        enderecoDTO1.setBairro(endereco1.getBairro());
        enderecoDTO1.setCidade(endereco1.getCidade());
        enderecoDTO1.setUf(endereco1.getUf());
        enderecoDTO1.setCep(endereco1.getCep());

        assertThat(enderecoController.criar(clienteId, enderecoDTO1).getStatusCode().is2xxSuccessful())
                .isTrue();

        // segundo endereço ativo para o mesmo cliente deve falhar
        EnderecoDTO enderecoDTO2 = new EnderecoDTO();
        Endereco endereco2 = novoEndereco("Rio de Janeiro");
        enderecoDTO2.setLogradouro(endereco2.getLogradouro());
        enderecoDTO2.setNumero(endereco2.getNumero());
        enderecoDTO2.setComplemento(endereco2.getComplemento());
        enderecoDTO2.setBairro(endereco2.getBairro());
        enderecoDTO2.setCidade(endereco2.getCidade());
        enderecoDTO2.setUf(endereco2.getUf());
        enderecoDTO2.setCep(endereco2.getCep());

        assertThatThrownBy(() -> enderecoController.criar(clienteId, enderecoDTO2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("já possui endereço ativo");
    }

    @Test
    void deveInativarEndereco_Logicamente() {
        Long clienteId = criarClientePF();
        Endereco endereco = novoEndereco("Recife");
        EnderecoDTO enderecoDTO = new EnderecoDTO();
        enderecoDTO.setLogradouro(endereco.getLogradouro());
        enderecoDTO.setNumero(endereco.getNumero());
        enderecoDTO.setComplemento(endereco.getComplemento());
        enderecoDTO.setBairro(endereco.getBairro());
        enderecoDTO.setCidade(endereco.getCidade());
        enderecoDTO.setUf(endereco.getUf());
        enderecoDTO.setCep(endereco.getCep());

        Endereco criado = enderecoController.criar(clienteId, enderecoDTO).getBody();
        assertThat(criado).isNotNull();

        // DELETE lógico
        var resp = enderecoController.remover(criado.getId());
        assertThat(resp.getStatusCode().is2xxSuccessful() || resp.getStatusCode().value() == 204).isTrue();

        // buscar e checar ativo=false
        Endereco apos = enderecoController.buscarPorId(criado.getId()).getBody();
        assertThat(apos).isNotNull();
        assertThat(apos.getAtivo()).isFalse();
    }
}