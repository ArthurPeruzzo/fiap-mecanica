package com.fiap.mecanica.ordemdeservico.infra.gateway.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "peca_id", nullable = false)
    private Long pecaId;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;
}
