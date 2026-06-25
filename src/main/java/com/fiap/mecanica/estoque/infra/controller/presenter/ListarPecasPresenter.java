package com.fiap.mecanica.estoque.infra.controller.presenter;

import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.usecase.ListarPecasOutputPort;
import com.fiap.mecanica.estoque.infra.controller.json.PecaResponseJson;
import com.fiap.mecanica.shared.page.Pagina;

public class ListarPecasPresenter implements ListarPecasOutputPort {

    private Pagina<PecaResponseJson> viewModel;

    @Override
    public void apresentar(Pagina<Peca> pagina) {
        this.viewModel = pagina.map(peca -> new PecaResponseJson(
                peca.getId(),
                peca.getNome(),
                peca.getDescricao(),
                peca.getPreco(),
                peca.getEstoqueTotal()
        ));
    }

    public Pagina<PecaResponseJson> getViewModel() {
        return viewModel;
    }
}
