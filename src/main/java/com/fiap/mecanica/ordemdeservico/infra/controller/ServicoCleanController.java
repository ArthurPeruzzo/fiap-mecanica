package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.ordemdeservico.core.dto.AtualizarServicoDto;
import com.fiap.mecanica.ordemdeservico.core.dto.CriarServicoDto;
import com.fiap.mecanica.ordemdeservico.core.dto.ListarServicosDto;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.servico.AtualizarServicoUseCase;
import com.fiap.mecanica.ordemdeservico.core.usecase.servico.CriarServicoUseCase;
import com.fiap.mecanica.ordemdeservico.core.usecase.servico.DeletarServicoUseCase;
import com.fiap.mecanica.ordemdeservico.core.usecase.servico.ListarServicosUseCase;
import com.fiap.mecanica.ordemdeservico.infra.controller.json.ServicoResponseJson;
import com.fiap.mecanica.ordemdeservico.infra.controller.presenter.ListarServicosPresenter;
import com.fiap.mecanica.shared.page.Pagina;

public class ServicoCleanController {

    private final ServicoGateway servicoGateway;

    public ServicoCleanController(ServicoGateway servicoGateway) {
        this.servicoGateway = servicoGateway;
    }

    public void criar(CriarServicoDto dto) {
        new CriarServicoUseCase(servicoGateway).criar(dto);
    }

    public Pagina<ServicoResponseJson> listar(ListarServicosDto dto) {
        var presenter = new ListarServicosPresenter();
        new ListarServicosUseCase(servicoGateway, presenter).listar(dto);
        return presenter.getViewModel();
    }

    public void atualizar(AtualizarServicoDto dto) {
        new AtualizarServicoUseCase(servicoGateway).atualizar(dto);
    }

    public void deletar(Long id) {
        new DeletarServicoUseCase(servicoGateway).deletar(id);
    }
}
