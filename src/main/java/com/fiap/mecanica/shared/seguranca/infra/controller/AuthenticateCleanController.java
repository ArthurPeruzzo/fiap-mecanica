package com.fiap.mecanica.shared.seguranca.infra.controller;

import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.AutenticacaoGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.UserGateway;
import com.fiap.mecanica.shared.seguranca.core.usecase.AuthenticateUserUseCase;
import com.fiap.mecanica.shared.seguranca.core.usecase.ConsultarOuCriarUsuarioClienteUseCase;
import com.fiap.mecanica.shared.seguranca.infra.controller.presenter.AutenticarPresenter;
import com.fiap.mecanica.shared.seguranca.infra.controller.presenter.ConsultarClientePresenter;

public class AuthenticateCleanController {

    private final AutenticacaoGateway autenticacaoGateway;
    private final TokenGateway tokenGateway;
    private final ClienteGateway clienteGateway;
    private final UserGateway userGateway;

    public AuthenticateCleanController(AutenticacaoGateway autenticacaoGateway,
                                        TokenGateway tokenGateway,
                                        ClienteGateway clienteGateway,
                                        UserGateway userGateway) {
        this.autenticacaoGateway = autenticacaoGateway;
        this.tokenGateway = tokenGateway;
        this.clienteGateway = clienteGateway;
        this.userGateway = userGateway;
    }

    public String login(String cpf, String senha) {
        var presenter = new AutenticarPresenter();
        new AuthenticateUserUseCase(autenticacaoGateway, tokenGateway, presenter).authenticate(cpf, senha);
        return presenter.getViewModel();
    }

    public Long consultarCliente(String cpf) {
        var presenter = new ConsultarClientePresenter();
        new ConsultarOuCriarUsuarioClienteUseCase(clienteGateway, userGateway, presenter).consultar(cpf);
        return presenter.getViewModel();
    }
}
