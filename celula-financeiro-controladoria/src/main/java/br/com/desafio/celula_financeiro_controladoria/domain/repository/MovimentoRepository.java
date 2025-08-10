package br.com.desafio.celula_financeiro_controladoria.domain.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.Movimento;

public interface MovimentoRepository extends JpaRepository<Movimento, Long> {

        @Query("select m from Movimento m where m.conta.cliente.id = :clienteId")
        List<Movimento> findByClienteId(Long clienteId);

        @Query("""
                        select m from Movimento m
                         where m.conta.cliente.id = :clienteId
                           and m.dataHora between :inicio and :fim
                        """)
        List<Movimento> findByClienteIdAndPeriodo(Long clienteId, LocalDateTime inicio, LocalDateTime fim);

        @Query("""
                        select m from Movimento m
                         where m.conta.cliente.id = :clienteId
                           and m.dataHora <= :ate
                        """)
        List<Movimento> findByClienteIdAte(Long clienteId, java.time.LocalDateTime ate);

}