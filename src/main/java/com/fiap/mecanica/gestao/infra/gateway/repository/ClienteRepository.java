package com.fiap.mecanica.gestao.infra.gateway.repository;

import com.fiap.mecanica.gestao.infra.gateway.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {
}
