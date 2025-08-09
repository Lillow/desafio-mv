package br.com.desafio.celula_financeiro_controladoria.domain.entity.Cliente;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.ClienteDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.base.BaseEntity;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoPessoa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.NoArgsConstructor;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
public abstract class Cliente extends BaseEntity {

        @Column(name = "NOME", nullable = false, length = 150)
        private String nome;

        @Column(name = "EMAIL", length = 120)
        private String email;

        @Column(name = "TELEFONE", length = 20)
        private String telefone;

        public Cliente(ClienteDTO clienteDTO) {
                this.nome = clienteDTO.getNome();
                this.email = clienteDTO.getEmail();
                this.telefone = clienteDTO.getTelefone();
        }

        public void toDTO(ClienteDTO clienteDTO) {
                clienteDTO.setNome(this.nome);
                clienteDTO.setEmail(this.email);
                clienteDTO.setTelefone(this.telefone);
        }

        public abstract ClienteDTO toDTO();

        /**
         * Fábrica: cria PF ou PJ com validações mínimas.
         * - Se dto.tipoPessoa == PF -> exige cpf
         * - Se dto.tipoPessoa == PJ -> exige cnpj e razaoSocial
         */
        public static Cliente fromDTO(ClienteDTO dto) {
                if (dto == null || dto.getTipoPessoa() == null) {
                        throw new IllegalArgumentException("Tipo de pessoa é obrigatório no DTO.");
                }
                if (dto.getTipoPessoa() == TipoPessoa.PF) {
                        if (dto.getCpf() == null || dto.getCpf().isBlank()) {
                                throw new IllegalArgumentException("CPF é obrigatório para pessoa física.");
                        }
                        return new ClientePF(dto);
                } else {
                        if (dto.getCnpj() == null || dto.getCnpj().isBlank()) {
                                throw new IllegalArgumentException("CNPJ é obrigatório para pessoa jurídica.");
                        }
                        if (dto.getRazaoSocial() == null || dto.getRazaoSocial().isBlank()) {
                                throw new IllegalArgumentException("Razão social é obrigatória para pessoa jurídica.");
                        }
                        return new ClientePJ(dto);
                }
        }

}
