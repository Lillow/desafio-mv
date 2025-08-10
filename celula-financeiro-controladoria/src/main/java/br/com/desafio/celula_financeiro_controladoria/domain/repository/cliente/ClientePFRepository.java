package br.com.desafio.celula_financeiro_controladoria.domain.repository.cliente;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.ClientePF;

@Repository
public interface ClientePFRepository extends JpaRepository<ClientePF, Long> {
    Optional<ClientePF> findByCpf(String cpf);
}
