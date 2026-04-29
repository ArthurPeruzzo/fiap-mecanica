package com.fiap.mecanica.gestao.core.gateway;

import com.fiap.mecanica.gestao.core.domain.Atendente;

import java.util.Optional;

public interface AtendenteGateway {

    Optional<Atendente> findById(Long id);
    Optional<Atendente> findByUsuarioId(Long usuarioId);
}
