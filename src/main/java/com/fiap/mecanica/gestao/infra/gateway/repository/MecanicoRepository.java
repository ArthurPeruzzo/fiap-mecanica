package com.fiap.mecanica.gestao.infra.gateway.repository;

import com.fiap.mecanica.gestao.infra.gateway.entity.MecanicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MecanicoRepository extends JpaRepository<MecanicoEntity, Long> {
    Optional<MecanicoEntity> findByUserId(Long userId);
}
