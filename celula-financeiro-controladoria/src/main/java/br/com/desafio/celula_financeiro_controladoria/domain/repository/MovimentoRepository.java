package br.com.desafio.celula_financeiro_controladoria.domain.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.Movimento;

public interface MovimentoRepository extends JpaRepository<Movimento, Long> {

  List<Movimento> findByContaId(Long contaId);

  List<Movimento> findByContaIdAndDataHoraBetween(Long contaId, OffsetDateTime ini, OffsetDateTime fim);

  long countByContaIdAndDataHoraBetween(Long contaId, OffsetDateTime ini, OffsetDateTime fim);

  @Query("""
      select coalesce(sum(case when m.tipo = 'CREDITO' then m.valor else 0 end), 0)
      from Movimento m
      where m.conta.id = :contaId
      """)
  BigDecimal sumCreditosByConta(Long contaId);

  @Query("""
      select coalesce(sum(case when m.tipo = 'DEBITO' then m.valor else 0 end), 0)
      from Movimento m
      where m.conta.id = :contaId
      """)
  BigDecimal sumDebitosByConta(Long contaId);

  @Query("""
      select coalesce(sum(case when m.tipo = 'CREDITO' then m.valor else 0 end), 0)
      from Movimento m
      where m.conta.id = :contaId and m.dataHora between :ini and :fim
      """)
  BigDecimal sumCreditosByContaAndPeriodo(Long contaId, OffsetDateTime ini, OffsetDateTime fim);

  @Query("""
      select coalesce(sum(case when m.tipo = 'DEBITO' then m.valor else 0 end), 0)
      from Movimento m
      where m.conta.id = :contaId and m.dataHora between :ini and :fim
      """)
  BigDecimal sumDebitosByContaAndPeriodo(Long contaId, OffsetDateTime ini, OffsetDateTime fim);

  @Query("""
      select m.conta.cliente.id, count(m)
      from Movimento m
      where m.dataHora between :ini and :fim
      group by m.conta.cliente.id
      """)
  List<Object[]> countPorClienteNoPeriodo(OffsetDateTime ini, OffsetDateTime fim);
}