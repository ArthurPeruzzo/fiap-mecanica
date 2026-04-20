package com.fiap.mecanica.gestao.infra.gateway.repository.persona;

import com.fiap.mecanica.gestao.infra.gateway.entity.persona.MecanicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MecanicoRepository extends JpaRepository<MecanicoEntity, Long> {
}
