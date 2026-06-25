package com.fiap.mecanica.estoque.infra.controller;

import com.fiap.mecanica.estoque.core.dto.AtualizarPecaDto;
import com.fiap.mecanica.estoque.core.dto.CriarPecaDto;
import com.fiap.mecanica.estoque.core.dto.ListarPecasDto;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.estoque.core.usecase.AtualizarPecaUseCase;
import com.fiap.mecanica.estoque.core.usecase.CriarPecaUseCase;
import com.fiap.mecanica.estoque.core.usecase.DeletarPecaUseCase;
import com.fiap.mecanica.estoque.core.usecase.ListarPecasUseCase;
import com.fiap.mecanica.estoque.infra.controller.json.PecaResponseJson;
import com.fiap.mecanica.estoque.infra.controller.presenter.ListarPecasPresenter;
import com.fiap.mecanica.shared.page.Pagina;

public class PecaCleanController {

    private final PecaGateway pecaGateway;

    public PecaCleanController(PecaGateway pecaGateway) {
        this.pecaGateway = pecaGateway;
    }

    public void criar(CriarPecaDto dto) {
        new CriarPecaUseCase(pecaGateway).criar(dto);
    }

    public Pagina<PecaResponseJson> listar(ListarPecasDto dto) {
        var presenter = new ListarPecasPresenter();
        new ListarPecasUseCase(pecaGateway, presenter).listar(dto);
        return presenter.getViewModel();
    }

    public void atualizar(AtualizarPecaDto dto) {
        new AtualizarPecaUseCase(pecaGateway).atualizar(dto);
    }

    public void deletar(Long id) {
        new DeletarPecaUseCase(pecaGateway).deletar(id);
    }
}
