package br.com.desafio.celula_financeiro_controladoria.domain.entity.base;

import java.time.Instant;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.base.BaseEntityDTO;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entidade base com auditoria simples e exclusão lógica.
 */
@MappedSuperclass
@NoArgsConstructor
public abstract class BaseEntity {

    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false)
    private Boolean ativo;

    public BaseEntity(BaseEntityDTO dto) {
        this.id = (dto != null && dto.getId() != null) ? dto.getId() : null;
        this.ativo = (dto != null && dto.getAtivo() != null) ? dto.getAtivo() : Boolean.TRUE;
    }

    public void toDTO(BaseEntityDTO dto) {
        dto.setAtivo(this.ativo);
    }

    public abstract BaseEntityDTO toDTO();

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (ativo == null)
            ativo = true;
    }

    @PreUpdate

    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
