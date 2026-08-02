package com.fiap.mecanica.shared.seguranca.core.gateway;


import com.fiap.mecanica.shared.seguranca.core.domain.User;

import java.util.Optional;

public interface UserGateway {

    Optional<User> findByCpf(String cpf);
    User create(User user);
}
