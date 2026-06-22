package com.fiap.mecanica.ordemdeservico.infra.gateway.repository;

import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.LinkAprovacaoOrcamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkAprovacaoOrcamentoRepository extends JpaRepository<LinkAprovacaoOrcamentoEntity, Long> {
    Optional<LinkAprovacaoOrcamentoEntity> findByOrdemServicoId(Long ordemServicoId);
}
