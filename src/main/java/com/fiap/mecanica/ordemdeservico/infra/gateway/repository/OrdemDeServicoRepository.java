package com.fiap.mecanica.ordemdeservico.infra.gateway.repository;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdemDeServicoRepository extends JpaRepository<OrdemDeServicoEntity, Long> {
    boolean existsByVeiculoIdAndStatusNotIn(Long veiculoId, List<StatusOrdemDeServico> statuses);

    @Query("SELECT o FROM OrdemDeServicoEntity o LEFT JOIN FETCH o.servicos WHERE o.id = :id")
    Optional<OrdemDeServicoEntity> findOrdemDeServicoById(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE OrdemDeServicoEntity o SET " +
           "o.mecanicoId = :mecanicoId, " +
           "o.status = :status, " +
           "o.dataInicioDiagnostico = :dataInicioDiagnostico, " +
           "o.dataConclusaoDiagnostico = :dataConclusaoDiagnostico " +
           "WHERE o.id = :id")
    void atualizar(
            @Param("id") Long id,
            @Param("mecanicoId") Long mecanicoId,
            @Param("status") StatusOrdemDeServico status,
            @Param("dataInicioDiagnostico") LocalDateTime dataInicioDiagnostico,
            @Param("dataConclusaoDiagnostico") LocalDateTime dataConclusaoDiagnostico
    );

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO ordem_servico_servico (ordem_servico_id, servico_id) VALUES (:ordemId, :servicoId)", nativeQuery = true)
    void vincularServico(@Param("ordemId") Long ordemId, @Param("servicoId") Long servicoId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM ordem_servico_servico WHERE ordem_servico_id = :ordemId AND servico_id = :servicoId", nativeQuery = true)
    void desvincularServico(@Param("ordemId") Long ordemId, @Param("servicoId") Long servicoId);
}
