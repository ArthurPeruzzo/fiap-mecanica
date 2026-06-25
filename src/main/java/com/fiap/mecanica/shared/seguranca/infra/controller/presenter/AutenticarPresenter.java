package com.fiap.mecanica.shared.seguranca.infra.controller.presenter;

import com.fiap.mecanica.shared.seguranca.core.usecase.AutenticarOutputPort;

public class AutenticarPresenter implements AutenticarOutputPort {

    private String viewModel;

    @Override
    public void apresentar(String token) {
        this.viewModel = token;
    }

    public String getViewModel() {
        return viewModel;
    }
}
