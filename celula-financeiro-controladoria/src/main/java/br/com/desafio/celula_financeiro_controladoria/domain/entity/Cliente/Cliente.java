package br.com.desafio.celula_financeiro_controladoria.domain.entity.cliente;

import java.util.List;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.ClienteDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.Conta;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.Endereco;
import br.com.desafio.celula_financeiro_controladoria.domain.entity.base.BaseEntity;
import br.com.desafio.celula_financeiro_controladoria.domain.enums.TipoPessoa;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
@Data
@lombok.EqualsAndHashCode(callSuper = true)
public abstract class Cliente extends BaseEntity {

        @Column(name = "NOME", nullable = false, length = 150)
        private String nome;

        @Column(name = "EMAIL", length = 120)
        private String email;

        @Column(name = "TELEFONE", length = 20)
        private String telefone;

        private List<Conta> contas;

        @Getter
        @OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        private Endereco endereco;

        public Cliente(ClienteDTO dto) {
                super(dto);
                this.nome = dto.getNome();
                this.email = dto.getEmail();
                this.telefone = dto.getTelefone();

                this.endereco = dto.getEndereco() != null ? new Endereco(dto.getEndereco()) : null;
        }

        public void toDTO(ClienteDTO dto) {
                super.toDTO(dto);
                dto.setNome(this.nome);
                dto.setEmail(this.email);
                dto.setTelefone(this.telefone);

                dto.setEndereco(this.endereco != null ? this.endereco.toDTO() : null);
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
