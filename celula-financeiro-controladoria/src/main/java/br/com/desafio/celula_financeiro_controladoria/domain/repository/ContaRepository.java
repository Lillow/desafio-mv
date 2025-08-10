package br.com.desafio.celula_financeiro_controladoria.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.Conta;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    List<Conta> findByClienteId(Long clienteId);

    @Query("select count(m) > 0 from Movimento m where m.conta.id = :contaId")
    boolean existsMovimentoByContaId(@Param("contaId") Long contaId);
}