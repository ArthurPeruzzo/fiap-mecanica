package com.fiap.mecanica.ordemdeservico.infra.gateway.entity;

import com.fiap.mecanica.estoque.infra.gateway.entity.InsumoEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Optional;

@Entity
@Table(name = "ordem_servico_insumo")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OrdemDeServicoInsumoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ordem_servico_id", nullable = false)
    private Long ordemServicoId;

    @ManyToOne
    @JoinColumn(name = "insumo_id", nullable = false)
    private InsumoEntity insumo;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    public Long getInsumoId() {
        return Optional.of(insumo).map(InsumoEntity::getId).orElse(null);
    }

    public BigDecimal getPreco() {
        return Optional.of(insumo).map(InsumoEntity::getPreco).orElse(null);
    }
}
