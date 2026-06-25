package com.fiap.mecanica.ordemdeservico.infra.controller.presenter;

import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.usecase.servico.ListarServicosOutputPort;
import com.fiap.mecanica.ordemdeservico.infra.controller.json.ServicoResponseJson;
import com.fiap.mecanica.shared.page.Pagina;

public class ListarServicosPresenter implements ListarServicosOutputPort {

    private Pagina<ServicoResponseJson> viewModel;

    @Override
    public void apresentar(Pagina<Servico> pagina) {
        this.viewModel = pagina.map(servico -> new ServicoResponseJson(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
                servico.getPreco()
        ));
    }

    public Pagina<ServicoResponseJson> getViewModel() {
        return viewModel;
    }
}
