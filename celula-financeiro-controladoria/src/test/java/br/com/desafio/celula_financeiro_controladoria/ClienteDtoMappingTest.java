package br.com.desafio.celula_financeiro_controladoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.ClienteDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.Cliente;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.ClientePF;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.ClientePJ;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoPessoa;

class ClienteDtoMappingTest {

    @Test
    void deveCriarPF_aPartirDoDTO_eVoltarParaDTO() {
        ClienteDTO dto = new ClienteDTO();
        dto.setTipoPessoa(TipoPessoa.PF);
        dto.setNome("Ana Maria");
        dto.setEmail("ana@exemplo.com");
        dto.setTelefone("85999998888");
        dto.setCpf("12345678901");
        dto.setRg("2001122");
        dto.setDataNascimento(LocalDate.of(1990, 1, 1));

        Cliente entidade = Cliente.fromDTO(dto);
        assertTrue(entidade instanceof ClientePF);

        ClienteDTO back = entidade.toDTO();
        assertEquals(TipoPessoa.PF, back.getTipoPessoa());
        assertEquals("Ana Maria", back.getNome());
        assertEquals("12345678901", back.getCpf());
        assertNull(back.getCnpj());
    }

    @Test
    void deveCriarPJ_aPartirDoDTO_eVoltarParaDTO() {
        ClienteDTO dto = new ClienteDTO();
        dto.setTipoPessoa(TipoPessoa.PJ);
        dto.setNome("Empresa XYZ");
        dto.setEmail("contato@xyz.com");
        dto.setTelefone("8533334444");
        dto.setCnpj("12345678000199");
        dto.setRazaoSocial("EMPRESA XYZ LTDA");
        dto.setInscricaoEstadual("ISENTO");

        Cliente entidade = Cliente.fromDTO(dto);
        assertTrue(entidade instanceof ClientePJ);

        ClienteDTO back = entidade.toDTO();
        assertEquals(TipoPessoa.PJ, back.getTipoPessoa());
        assertEquals("EMPRESA XYZ LTDA", back.getRazaoSocial());
        assertEquals("12345678000199", back.getCnpj());
        assertNull(back.getCpf());
    }

    @Test
    void deveFalharSeTipoPessoaNulo() {
        ClienteDTO dto = new ClienteDTO();
        var ex = assertThrows(IllegalArgumentException.class, () -> Cliente.fromDTO(dto));
        assertTrue(ex.getMessage().contains("Tipo de pessoa é obrigatório"));
    }

    @Test
    void deveFalharPF_SemCPF() {
        ClienteDTO dto = new ClienteDTO();
        dto.setTipoPessoa(TipoPessoa.PF);
        dto.setNome("João");
        var ex = assertThrows(IllegalArgumentException.class, () -> Cliente.fromDTO(dto));
        assertTrue(ex.getMessage().contains("CPF é obrigatório"));
    }

    @Test
    void deveFalharPJ_SemCNPJ_ouSemRazaoSocial() {
        ClienteDTO dto = new ClienteDTO();
        dto.setTipoPessoa(TipoPessoa.PJ);
        dto.setNome("Empresa");
        // sem CNPJ
        var ex1 = assertThrows(IllegalArgumentException.class, () -> Cliente.fromDTO(dto));
        assertTrue(ex1.getMessage().contains("CNPJ é obrigatório"));

        dto.setCnpj("12345678000199");
        // sem razão social
        var ex2 = assertThrows(IllegalArgumentException.class, () -> Cliente.fromDTO(dto));
        assertTrue(ex2.getMessage().contains("Razão social é obrigatória"));
    }
}