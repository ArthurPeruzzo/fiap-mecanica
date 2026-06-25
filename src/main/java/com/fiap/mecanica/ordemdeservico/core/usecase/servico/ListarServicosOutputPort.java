package com.fiap.mecanica.ordemdeservico.core.usecase.servico;

import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.shared.page.Pagina;

public interface ListarServicosOutputPort {
    void apresentar(Pagina<Servico> pagina);
}
