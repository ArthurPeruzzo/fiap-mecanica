package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.dto.ConsultarStatusOrdemDeServicoDto;

public interface ConsultarStatusOrdemDeServicoOutputPort {
    void apresentar(ConsultarStatusOrdemDeServicoDto dto);
}
