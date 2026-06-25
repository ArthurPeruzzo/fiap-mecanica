package com.fiap.mecanica.gestao.infra.controller.presenter;

import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.gestao.core.usecase.ListarVeiculosOutputPort;
import com.fiap.mecanica.gestao.infra.controller.json.VeiculoResponseJson;
import com.fiap.mecanica.shared.page.Pagina;

public class ListarVeiculosPresenter implements ListarVeiculosOutputPort {

    private Pagina<VeiculoResponseJson> viewModel;

    @Override
    public void apresentar(Pagina<Veiculo> pagina) {
        this.viewModel = pagina.map(veiculo -> new VeiculoResponseJson(
                veiculo.getId(),
                veiculo.getClienteId(),
                veiculo.getPlaca().getValorFormatado(),
                veiculo.getModelo(),
                veiculo.getAno()
        ));
    }

    public Pagina<VeiculoResponseJson> getViewModel() {
        return viewModel;
    }
}
