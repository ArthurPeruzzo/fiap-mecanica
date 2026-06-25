package com.fiap.mecanica.ordemdeservico.infra.controller.presenter;

import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.CriarOrdemDeServicoOutputPort;

public class CriarOrdemDeServicoPresenter implements CriarOrdemDeServicoOutputPort {

    private Long viewModel;

    @Override
    public void apresentar(Long id) {
        this.viewModel = id;
    }

    public Long getViewModel() {
        return viewModel;
    }
}
