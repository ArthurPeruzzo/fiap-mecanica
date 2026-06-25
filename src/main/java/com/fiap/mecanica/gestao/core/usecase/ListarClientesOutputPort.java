package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.shared.page.Pagina;

public interface ListarClientesOutputPort {
    void apresentar(Pagina<Cliente> pagina);
}
