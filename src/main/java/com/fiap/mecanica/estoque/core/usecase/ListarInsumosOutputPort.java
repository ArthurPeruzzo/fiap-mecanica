package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.shared.page.Pagina;

public interface ListarInsumosOutputPort {
    void apresentar(Pagina<Insumo> pagina);
}
