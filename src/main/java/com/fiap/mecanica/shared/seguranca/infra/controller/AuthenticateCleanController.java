package com.fiap.mecanica.shared.seguranca.infra.controller;

import com.fiap.mecanica.shared.seguranca.core.gateway.AutenticacaoGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import com.fiap.mecanica.shared.seguranca.core.usecase.AuthenticateUserUseCase;
import com.fiap.mecanica.shared.seguranca.infra.controller.presenter.AutenticarPresenter;

public class AuthenticateCleanController {

    private final AutenticacaoGateway autenticacaoGateway;
    private final TokenGateway tokenGateway;

    public AuthenticateCleanController(AutenticacaoGateway autenticacaoGateway, TokenGateway tokenGateway) {
        this.autenticacaoGateway = autenticacaoGateway;
        this.tokenGateway = tokenGateway;
    }

    public String login(String cpf, String senha) {
        var presenter = new AutenticarPresenter();
        new AuthenticateUserUseCase(autenticacaoGateway, tokenGateway, presenter).authenticate(cpf, senha);
        return presenter.getViewModel();
    }
}
