package com.fiap.mecanica.shared.seguranca.core.usecase;

import com.fiap.mecanica.shared.seguranca.core.gateway.AutenticacaoGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;

public class AuthenticateUserUseCase {

    private final AutenticacaoGateway autenticacaoGateway;
    private final TokenGateway tokenGateway;
    private final AutenticarOutputPort outputPort;

    public AuthenticateUserUseCase(AutenticacaoGateway autenticacaoGateway, TokenGateway tokenGateway,
                                   AutenticarOutputPort outputPort) {
        this.autenticacaoGateway = autenticacaoGateway;
        this.tokenGateway = tokenGateway;
        this.outputPort = outputPort;
    }

    public void authenticate(String email, String senha) {
        var user = autenticacaoGateway.autenticar(email, senha);
        outputPort.apresentar(tokenGateway.generateToken(user));
    }
}
