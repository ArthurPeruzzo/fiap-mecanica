package com.fiap.mecanica.ordemdeservico.infra.controller.presenter;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.ConsultarStatusOrdemDeServicoOutputPort;
import com.fiap.mecanica.ordemdeservico.infra.controller.json.StatusOrdemDeServicoResponseJson;

public class ConsultarStatusOrdemDeServicoPresenter implements ConsultarStatusOrdemDeServicoOutputPort {

    private StatusOrdemDeServicoResponseJson viewModel;

    @Override
    public void apresentar(OrdemDeServico ordemDeServico) {
        this.viewModel = new StatusOrdemDeServicoResponseJson(ordemDeServico.getId(), ordemDeServico.getStatus().name());
    }

    public StatusOrdemDeServicoResponseJson getViewModel() {
        return viewModel;
    }
}
