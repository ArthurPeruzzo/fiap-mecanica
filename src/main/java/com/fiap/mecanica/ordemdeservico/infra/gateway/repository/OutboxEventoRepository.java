package com.fiap.mecanica.ordemdeservico.infra.gateway.repository;

import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OutboxEventoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventoRepository extends JpaRepository<OutboxEventoEntity, String> {

    /**
     * Pendentes em ordem de criação. O SKIP LOCKED faz cada réplica do monólito pegar linhas diferentes
     * (o HPA pode ter até 4); só funciona dentro de uma transação, que segura os locks até o commit.
     */
    @Query(value = "SELECT * FROM outbox_evento WHERE enviado_em IS NULL ORDER BY criado_em, id LIMIT :limite FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    List<OutboxEventoEntity> buscarPendentesParaEnvio(@Param("limite") int limite);

    long countByEnviadoEmIsNull();
}
