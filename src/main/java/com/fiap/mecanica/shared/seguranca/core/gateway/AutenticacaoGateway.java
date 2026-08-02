package com.fiap.mecanica.shared.seguranca.core.gateway;

import com.fiap.mecanica.shared.seguranca.core.domain.User;

public interface AutenticacaoGateway {
    User autenticar(String cpf, String senha);
}
