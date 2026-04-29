package com.fiap.mecanica.ordemdeservico.infra.gateway.repository;

import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.ServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicoRepository extends JpaRepository<ServicoEntity, Long> {
}
