package br.com.desafio.celula_financeiro_controladoria.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.desafio.celula_financeiro_controladoria.domain.entity.Endereco;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    Optional<Endereco> findByClienteId(Long clienteId);

    Optional<Endereco> findByIdAndAtivoTrue(Long id);

    boolean existsByClienteIdAndAtivoTrue(Long clienteId);
}
