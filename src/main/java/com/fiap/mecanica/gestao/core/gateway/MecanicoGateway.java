package com.fiap.mecanica.gestao.core.gateway;

import com.fiap.mecanica.gestao.core.domain.Mecanico;

import java.util.Optional;

public interface MecanicoGateway {

    Optional<Mecanico> findById(Long id);
}
