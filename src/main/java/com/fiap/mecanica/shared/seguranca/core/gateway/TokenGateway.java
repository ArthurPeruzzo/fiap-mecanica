package com.fiap.mecanica.shared.seguranca.core.gateway;

import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.core.domain.User;

import java.util.List;

public interface TokenGateway {
    String generateToken(User user);
    String getEmail();
    Long getUserId();
    List<RoleEnum> getRoles();
}
