package com.fiap.mecanica.ordemdeservico.infra.gateway.repository;

import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdemDeServicoServicoRepository extends JpaRepository<OrdemDeServicoServicoEntity, Long> {

	List<OrdemDeServicoServicoEntity> findByOrdemServicoId(Long ordemServicoId);

	void deleteByOrdemServicoIdAndServicoId(Long ordemServicoId, Long servicoId);

}
