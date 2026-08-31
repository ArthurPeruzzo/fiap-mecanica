package com.fiap.mecanica.shared.seguranca.infra.controller.presenter;

import com.fiap.mecanica.shared.seguranca.core.usecase.ConsultarClienteOutputPort;

public class ConsultarClientePresenter implements ConsultarClienteOutputPort {

    private Long viewModel;

    @Override
    public void apresentar(Long userId) {
        this.viewModel = userId;
    }

    public Long getViewModel() {
        return viewModel;
    }
}
