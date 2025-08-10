package br.com.desafio.celula_financeiro_controladoria.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.Conta;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    @Query("SELECT c FROM Conta c WHERE c.agencia = :agencia AND c.numero = :numero")
    Optional<Conta> findByAgenciaAndNumero(String agencia, String numero);

    @Query("SELECT c FROM Conta c WHERE c.cliente.id = :clienteId")
    List<Conta> findByClienteId(Long clienteId);

    @Query("SELECT c FROM Conta c WHERE c.documento = :documento")
    List<Conta> findByDocumento(String documento);
}
