package com.fiap.mecanica.ordemdeservico.infra.gateway.entity;

import com.fiap.mecanica.estoque.infra.gateway.entity.PecaEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Optional;

@Entity
@Table(name = "ordem_servico_peca")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OrdemDeServicoPecaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ordem_servico_id", nullable = false)
    private Long ordemServicoId;

    @ManyToOne
    @JoinColumn(name = "peca_id", nullable = false)
    private PecaEntity peca;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    public Long getPecaId() {
        return Optional.of(peca).map(PecaEntity::getId).orElse(null);
    }

    public BigDecimal getPreco() {
        return Optional.of(peca).map(PecaEntity::getPreco).orElse(null);
    }
}
