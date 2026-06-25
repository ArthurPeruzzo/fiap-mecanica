package com.fiap.mecanica.gestao.infra.controller;

import com.fiap.mecanica.gestao.core.dto.AtualizarVeiculoDto;
import com.fiap.mecanica.gestao.core.dto.CriarVeiculoDto;
import com.fiap.mecanica.gestao.core.dto.ListarVeiculosDto;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import com.fiap.mecanica.gestao.core.usecase.AtualizarVeiculoUseCase;
import com.fiap.mecanica.gestao.core.usecase.CriarVeiculoUseCase;
import com.fiap.mecanica.gestao.core.usecase.DeletarVeiculoUseCase;
import com.fiap.mecanica.gestao.core.usecase.ListarVeiculosUseCase;
import com.fiap.mecanica.gestao.infra.controller.json.VeiculoResponseJson;
import com.fiap.mecanica.gestao.infra.controller.presenter.ListarVeiculosPresenter;
import com.fiap.mecanica.shared.page.Pagina;

public class VeiculoCleanController {

    private final VeiculoGateway veiculoGateway;
    private final ClienteGateway clienteGateway;

    public VeiculoCleanController(VeiculoGateway veiculoGateway, ClienteGateway clienteGateway) {
        this.veiculoGateway = veiculoGateway;
        this.clienteGateway = clienteGateway;
    }

    public void criar(CriarVeiculoDto dto) {
        new CriarVeiculoUseCase(veiculoGateway, clienteGateway).criar(dto);
    }

    public Pagina<VeiculoResponseJson> listar(ListarVeiculosDto dto) {
        var presenter = new ListarVeiculosPresenter();
        new ListarVeiculosUseCase(veiculoGateway, presenter).listar(dto);
        return presenter.getViewModel();
    }

    public void atualizar(AtualizarVeiculoDto dto) {
        new AtualizarVeiculoUseCase(veiculoGateway).atualizar(dto);
    }

    public void deletar(Long id) {
        new DeletarVeiculoUseCase(veiculoGateway).deletar(id);
    }
}
