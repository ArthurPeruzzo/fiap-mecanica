package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.dto.OrdemDeServicoListagemDto;
import com.fiap.mecanica.shared.page.Pagina;

public interface ListarOrdemDeServicoOutputPort {
    void apresentar(Pagina<OrdemDeServicoListagemDto> pagina);
}
