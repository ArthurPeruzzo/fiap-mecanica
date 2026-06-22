package com.fiap.mecanica.ordemdeservico.infra.gateway.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "link_aprovacao_orcamento")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class LinkAprovacaoOrcamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ordem_servico_id", nullable = false, unique = true)
    private Long ordemServicoId;

    @Column(name = "token", nullable = false, unique = true, length = 36)
    private String token;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(name = "data_utilizacao")
    private LocalDateTime dataUtilizacao;
}
