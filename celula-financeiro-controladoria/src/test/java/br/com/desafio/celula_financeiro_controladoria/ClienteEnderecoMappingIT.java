package br.com.desafio.celula_financeiro_controladoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.ClienteDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.dto.EnderecoDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.ClientePF;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoPessoa;
import br.com.desafio.celula_financeiro_controladoria.domain.repository.cliente.ClientePFRepository;
import jakarta.persistence.EntityManager;

@DataJpaTest
class ClienteEnderecoMappingIT {

    @Autowired
    ClientePFRepository clientePFRepository;
    @Autowired
    EntityManager em;

    @Test
    void devePersistirClienteComEndereco_OneToOne() {
        var dtoEnd = new EnderecoDTO();
        dtoEnd.setLogradouro("Rua A");
        dtoEnd.setNumero("100");
        dtoEnd.setCidade("Fortaleza");
        dtoEnd.setUf("CE");
        dtoEnd.setCep("60000000");

        var dtoCli = new ClienteDTO();
        dtoCli.setTipoPessoa(TipoPessoa.PF);
        dtoCli.setNome("Ana");
        dtoCli.setCpf("12345678901");
        dtoCli.setEndereco(dtoEnd);

        var pf = new ClientePF(dtoCli);
        pf = clientePFRepository.saveAndFlush(pf);
        em.clear();

        var loaded = clientePFRepository.findById(pf.getId()).orElseThrow();
        assertNotNull(loaded.getEndereco());
        assertEquals("Rua A", loaded.getEndereco().getLogradouro());

    }
}
