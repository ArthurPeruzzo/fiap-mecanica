package com.fiap.mecanica.shared.seguranca.core.gateway;


import com.fiap.mecanica.shared.seguranca.core.domain.User;

import java.util.Optional;

public interface UserGateway {

    Optional<User> findByCpf(String cpf);
    Optional<User> findById(Long id);
    User create(User user);
}
