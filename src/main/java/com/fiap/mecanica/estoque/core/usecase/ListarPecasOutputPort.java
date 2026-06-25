package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.shared.page.Pagina;

public interface ListarPecasOutputPort {
    void apresentar(Pagina<Peca> pagina);
}
