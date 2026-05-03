package com.fiap.mecanica.ordemdeservico.infra.gateway.repository;

import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrdemDeServicoServicoRepository extends JpaRepository<OrdemDeServicoServicoEntity, Long> {

	List<OrdemDeServicoServicoEntity> findByOrdemServicoId(Long ordemServicoId);

	void deleteByOrdemServicoIdAndServicoId(Long ordemServicoId, Long servicoId);


	@Modifying
	@Transactional
	@Query("UPDATE OrdemDeServicoServicoEntity o SET " +
			"o.status = :status, " +
			"o.dataInicioExecucao = :dataInicioExecucao, " +
			"o.dataFimExecucao = :dataFimExecucao " +
			"WHERE o.servicoId = :servicoId " +
			"AND o.ordemServicoId = :ordemServicoId")
	void atualizar(
			@Param("servicoId") Long servicoId,
			@Param("ordemServicoId") Long ordemServicoId,
			@Param("status") StatusServico status,
			@Param("dataInicioExecucao") LocalDateTime dataInicioExecucao,
			@Param("dataFimExecucao") LocalDateTime dataFimExecucao
	);
}
