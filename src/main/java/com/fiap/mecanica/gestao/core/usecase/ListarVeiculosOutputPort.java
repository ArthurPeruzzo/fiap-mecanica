package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.shared.page.Pagina;

public interface ListarVeiculosOutputPort {
    void apresentar(Pagina<Veiculo> pagina);
}
