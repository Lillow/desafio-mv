package br.com.desafio.celula_financeiro_controladoria.domain.repository.cliente;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.ClientePJ;

@Repository
public interface ClientePJRepository extends JpaRepository<ClientePJ, Long> {
    Optional<ClientePJ> findByCnpj(String cnpj);
}
