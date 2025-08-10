package br.com.desafio.celula_financeiro_controladoria.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.Conta;

@Repository
public interface ContaRepository extends JpaRepository<Conta, Long> {

    List<Conta> findByClienteId(Long clienteId);

    boolean existsByIdAndMovimentosIsNotEmpty(Long id);
}
