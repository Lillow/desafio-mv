package br.com.desafio.celula_financeiro_controladoria.domain.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.Movimento;

public interface MovimentoRepository extends JpaRepository<Movimento, Long> {
    @Query("""
                select m from Movimento m
                where m.conta.id = :contaId
                  and m.dataHora between :ini and :fim
                order by m.dataHora asc
            """)
    List<Movimento> findByContaAndPeriodo(Long contaId, LocalDateTime ini, LocalDateTime fim);

    @Query("""
                select count(m) from Movimento m
                where m.conta.cliente.id = :clienteId
                  and m.dataHora between :ini and :fim
            """)
    long countByClienteNoPeriodo(Long clienteId, LocalDateTime ini, LocalDateTime fim);

    @Query("""
                select m from Movimento m
                where m.conta.cliente.id = :clienteId
                  and m.dataHora between :ini and :fim
            """)
    List<Movimento> findByClienteAndPeriodo(Long clienteId, LocalDateTime ini, LocalDateTime fim);
}
