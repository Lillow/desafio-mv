package br.com.desafio.celula_financeiro_controladoria.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.Conta;

@Repository
public interface ContaRepository extends JpaRepository<Conta, Long> {

    @Query("select count(m)>0 from Movimento m where m.conta.id = :contaId")
    boolean existsMovimentoByContaId(Long contaId);
}
