package com.fiap.mecanica.gestao.infra.controller.presenter;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.usecase.ListarClientesOutputPort;
import com.fiap.mecanica.gestao.infra.controller.json.ClienteResponseJson;
import com.fiap.mecanica.shared.page.Pagina;
import com.fiap.mecanica.shared.valueobjects.Cnpj;
import com.fiap.mecanica.shared.valueobjects.Cpf;

public class ListarClientesPresenter implements ListarClientesOutputPort {

    private Pagina<ClienteResponseJson> viewModel;

    @Override
    public void apresentar(Pagina<Cliente> pagina) {
        this.viewModel = pagina.map(cliente -> new ClienteResponseJson(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf().map(Cpf::getValorFormatado).orElse(null),
                cliente.getCnpj().map(Cnpj::getValorFormatado).orElse(null)
        ));
    }

    public Pagina<ClienteResponseJson> getViewModel() {
        return viewModel;
    }
}
