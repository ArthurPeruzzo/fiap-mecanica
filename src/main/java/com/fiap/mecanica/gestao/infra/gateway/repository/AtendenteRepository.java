package com.fiap.mecanica.gestao.infra.gateway.repository;

import com.fiap.mecanica.gestao.infra.gateway.entity.AtendenteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AtendenteRepository extends JpaRepository<AtendenteEntity, Long> {
	Optional<AtendenteEntity> findByUserId(Long userId);
}
