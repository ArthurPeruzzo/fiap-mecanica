package com.fiap.mecanica.ordemdeservico.infra.gateway.repository;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrdemDeServicoRepository extends JpaRepository<OrdemDeServicoEntity, Long> {
    boolean existsByVeiculoIdAndStatusNotIn(Long veiculoId, List<StatusOrdemDeServico> statuses);

    @Modifying
    @Transactional
    @Query("UPDATE OrdemDeServicoEntity o SET " +
           "o.mecanicoId = :mecanicoId, " +
           "o.status = :status, " +
           "o.dataInicioDiagnostico = :dataInicioDiagnostico, " +
           "o.dataConclusaoDiagnostico = :dataConclusaoDiagnostico, " +
           "o.orcamentoTotal = :orcamentoTotal, " +
           "o.dataEnvioOrcamento = :dataEnvioOrcamento, " +
            "o.dataCancelamento = :dataCancelamento, " +
            "o.dataAprovacao = :dataAprovacao, " +
            "o.dataFinalizacao = :dataFinalizacao, " +
            "o.dataEntrega = :dataEntrega " +
           "WHERE o.id = :id")
    void atualizar(
            @Param("id") Long id,
            @Param("mecanicoId") Long mecanicoId,
            @Param("status") StatusOrdemDeServico status,
            @Param("dataInicioDiagnostico") LocalDateTime dataInicioDiagnostico,
            @Param("dataConclusaoDiagnostico") LocalDateTime dataConclusaoDiagnostico,
            @Param("orcamentoTotal") BigDecimal orcamentoTotal,
            @Param("dataEnvioOrcamento") LocalDateTime dataEnvioOrcamento,
            @Param("dataCancelamento") LocalDateTime dataCancelamento,
            @Param("dataAprovacao") LocalDateTime dataAprovacao,
            @Param("dataFinalizacao") LocalDateTime dataFinalizacao,
            @Param("dataEntrega") LocalDateTime dataEntrega
    );

    @Query(
        value = """
            SELECT * FROM ordem_servico
            WHERE status NOT IN ('FINALIZADA', 'ENTREGUE', 'CANCELADA')
            ORDER BY
              CASE status
                WHEN 'EM_EXECUCAO'           THEN 1
                WHEN 'AGUARDANDO_APROVACAO'  THEN 2
                WHEN 'EM_DIAGNOSTICO'        THEN 3
                WHEN 'DIAGNOSTICO_CONCLUIDO' THEN 4
                WHEN 'RECEBIDA'              THEN 5
              END ASC,
              data_criacao ASC
            """,
        countQuery = """
            SELECT COUNT(*) FROM ordem_servico
            WHERE status NOT IN ('FINALIZADA', 'ENTREGUE', 'CANCELADA')
            """,
        nativeQuery = true
    )
    Page<OrdemDeServicoEntity> buscaOrdemDeServicos(Pageable pageable);

    @Query(
        value = "SELECT * FROM ordem_servico WHERE cliente_id = :clienteId ORDER BY data_criacao DESC",
        countQuery = "SELECT COUNT(*) FROM ordem_servico WHERE cliente_id = :clienteId",
        nativeQuery = true
    )
    Page<OrdemDeServicoEntity> buscaOrdemDeServicosPorCliente(@Param("clienteId") Long clienteId, Pageable pageable);

}
