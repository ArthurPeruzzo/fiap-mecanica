package com.fiap.mecanica.ordemdeservico.infra.controller.presenter;

import com.fiap.mecanica.ordemdeservico.core.dto.ConsultarStatusOrdemDeServicoDto;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.ConsultarStatusOrdemDeServicoOutputPort;
import com.fiap.mecanica.ordemdeservico.infra.controller.json.StatusOrdemDeServicoResponseJson;

public class ConsultarStatusOrdemDeServicoPresenter implements ConsultarStatusOrdemDeServicoOutputPort {

    private StatusOrdemDeServicoResponseJson viewModel;

    @Override
    public void apresentar(ConsultarStatusOrdemDeServicoDto dto) {
        this.viewModel = new StatusOrdemDeServicoResponseJson(dto.id(), dto.status());
    }

    public StatusOrdemDeServicoResponseJson getViewModel() {
        return viewModel;
    }
}
