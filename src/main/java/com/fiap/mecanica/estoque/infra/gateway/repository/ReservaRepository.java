package com.fiap.mecanica.estoque.infra.gateway.repository;

import com.fiap.mecanica.estoque.core.domain.StatusReserva;
import com.fiap.mecanica.estoque.core.domain.TipoItemReserva;
import com.fiap.mecanica.estoque.infra.gateway.entity.ReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<ReservaEntity, Long> {
	Optional<ReservaEntity> findByOrdemServicoIdAndTipoItemAndItemId(Long ordemServicoId, TipoItemReserva tipoItem, Long itemId);
	List<ReservaEntity> findByOrdemServicoIdAndStatus(Long ordemServicoId, StatusReserva status);
}
