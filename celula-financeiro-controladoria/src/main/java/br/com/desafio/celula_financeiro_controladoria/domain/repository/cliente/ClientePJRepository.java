package br.com.desafio.celula_financeiro_controladoria.domain.repository.cliente;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente.ClientePF;

@Repository
public interface ClientePJRepository extends JpaRepository<ClientePF, Long> {
    @Query("SELECT c FROM ClientePF c WHERE c.cnpj = :cnpj")
    Optional<ClientePF> findByCnpj(String cnpj);
}
