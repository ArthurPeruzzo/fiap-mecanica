package com.fiap.mecanica.ordemdeservico.infra.gateway.repository;

import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoInsumoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdemDeServicoInsumoRepository extends JpaRepository<OrdemDeServicoInsumoEntity, Long> {

    List<OrdemDeServicoInsumoEntity> findByOrdemServicoId(Long ordemServicoId);

    Optional<OrdemDeServicoInsumoEntity> findByOrdemServicoIdAndInsumoId(Long ordemServicoId, Long insumoId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE ordem_servico_insumo SET quantidade = quantidade + :quantidade WHERE ordem_servico_id = :ordemServicoId AND insumo_id = :insumoId", nativeQuery = true)
    void somarQuantidade(
            @Param("ordemServicoId") Long ordemServicoId,
            @Param("insumoId") Long insumoId,
            @Param("quantidade") Integer quantidade
    );

    @Modifying
    @Transactional
    @Query(value = "UPDATE ordem_servico_insumo SET quantidade = quantidade - :quantidade WHERE ordem_servico_id = :ordemServicoId AND insumo_id = :insumoId", nativeQuery = true)
    void diminuirQuantidade(
            @Param("ordemServicoId") Long ordemServicoId,
            @Param("insumoId") Long insumoId,
            @Param("quantidade") Integer quantidade
    );
}
