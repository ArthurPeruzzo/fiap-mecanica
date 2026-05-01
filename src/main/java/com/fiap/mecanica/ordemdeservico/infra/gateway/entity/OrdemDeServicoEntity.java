package com.fiap.mecanica.ordemdeservico.infra.gateway.entity;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordem_servico")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OrdemDeServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "veiculo_id", nullable = false)
    private Long veiculoId;

    @Column(name = "atendente_id", nullable = false)
    private Long atendenteId;

    @Column(name = "mecanico_id")
    private Long mecanicoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusOrdemDeServico status;

    @Column(name = "descricao", nullable = false)
    private String descricao;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_inicio_diagnostico")
    private LocalDateTime dataInicioDiagnostico;

    @Column(name = "data_conclusao_diagnostico")
    private LocalDateTime dataConclusaoDiagnostico;

    @Column(name = "data_envio_orcamento")
    private LocalDateTime dataEnvioOrcamento;

    @Column(name = "orcamento_total")
    private BigDecimal orcamentoTotal;

    @Column(name = "data_cancelamento")
    private LocalDateTime dataCancelamento;

    @Column(name = "data_aprovacao")
    private LocalDateTime dataAprovacao;

}
