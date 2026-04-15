package com.fiap.mecanica.shared.seguranca.infra.token;

import com.fiap.mecanica.shared.seguranca.infra.token.dto.TokenParams;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;

import java.util.List;

public interface TokenGateway {
    String generateToken(TokenParams params);
    String getEmail();
    Long getUserId();
    List<RoleEnum> getRoles();
}
