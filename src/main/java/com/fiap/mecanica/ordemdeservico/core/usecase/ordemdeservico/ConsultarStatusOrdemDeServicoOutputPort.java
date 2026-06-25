package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;

public interface ConsultarStatusOrdemDeServicoOutputPort {
    void apresentar(OrdemDeServico ordemDeServico);
}
