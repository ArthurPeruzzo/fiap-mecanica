package com.fiap.mecanica.gestao.core.gateway.persona;

import com.fiap.mecanica.gestao.core.domain.persona.Mecanico;

import java.util.Optional;

public interface MecanicoGateway {

    Optional<Mecanico> findById(Long id);
}
