package com.fiap.mecanica.gestao.core.gateway.persona;

import com.fiap.mecanica.gestao.core.domain.persona.Atendente;

import java.util.Optional;

public interface AtendenteGateway {

    Optional<Atendente> findById(Long id);
}
