package com.fiap.mecanica.gestao.infra.controller;

import com.fiap.mecanica.gestao.core.dto.AtualizarClienteDto;
import com.fiap.mecanica.gestao.core.dto.CriarClienteDto;
import com.fiap.mecanica.gestao.core.dto.ListarClientesDto;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.gestao.core.usecase.AtualizarClienteUseCase;
import com.fiap.mecanica.gestao.core.usecase.CriarClienteUseCase;
import com.fiap.mecanica.gestao.core.usecase.DeletarClienteUseCase;
import com.fiap.mecanica.gestao.core.usecase.ListarClientesUseCase;
import com.fiap.mecanica.gestao.infra.controller.json.ClienteResponseJson;
import com.fiap.mecanica.gestao.infra.controller.presenter.ListarClientesPresenter;
import com.fiap.mecanica.shared.page.Pagina;

public class ClienteCleanController {

    private final ClienteGateway clienteGateway;

    public ClienteCleanController(ClienteGateway clienteGateway) {
        this.clienteGateway = clienteGateway;
    }

    public void criar(CriarClienteDto dto) {
        new CriarClienteUseCase(clienteGateway).criar(dto);
    }

    public Pagina<ClienteResponseJson> listar(ListarClientesDto dto) {
        var presenter = new ListarClientesPresenter();
        new ListarClientesUseCase(clienteGateway, presenter).listar(dto);
        return presenter.getViewModel();
    }

    public void atualizar(AtualizarClienteDto dto) {
        new AtualizarClienteUseCase(clienteGateway).atualizar(dto);
    }

    public void deletar(Long id) {
        new DeletarClienteUseCase(clienteGateway).deletar(id);
    }
}
