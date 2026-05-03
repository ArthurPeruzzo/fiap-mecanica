package com.fiap.mecanica.ordemdeservico.infra.gateway.repository;

import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoPecaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdemDeServicoPecaRepository extends JpaRepository<OrdemDeServicoPecaEntity, Long> {

    List<OrdemDeServicoPecaEntity> findByOrdemServicoId(Long ordemServicoId);

    Optional<OrdemDeServicoPecaEntity> findByOrdemServicoIdAndPecaId(Long ordemServicoId, Long id);



    @Modifying
    @Transactional
    @Query(value = "UPDATE ordem_servico_peca SET quantidade = quantidade + :quantidade WHERE ordem_servico_id = :ordemServicoId AND peca_id = :pecaId", nativeQuery = true)
    void somarQuantidade(
            @Param("ordemServicoId") Long ordemServicoId,
            @Param("pecaId") Long pecaId,
            @Param("quantidade") Integer quantidade
    );

    @Modifying
    @Transactional
    @Query(value = "UPDATE ordem_servico_peca SET quantidade = quantidade - :quantidade WHERE ordem_servico_id = :ordemServicoId AND peca_id = :pecaId", nativeQuery = true)
    void diminuirQuantidade(
            @Param("ordemServicoId") Long ordemServicoId,
            @Param("pecaId") Long pecaId,
            @Param("quantidade") Integer quantidade
    );

}
