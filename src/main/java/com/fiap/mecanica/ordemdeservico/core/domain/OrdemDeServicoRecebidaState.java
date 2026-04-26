package com.fiap.mecanica.ordemdeservico.core.domain;

import java.time.LocalDateTime;

class OrdemDeServicoRecebidaState implements OrdemDeServicoState {

    @Override
    public void iniciarDiagnostico(OrdemDeServico os, Long mecanicoId) {
        os.setMecanicoId(mecanicoId);
        os.setDataInicioDiagnostico(LocalDateTime.now());
        os.transicionarPara(StatusOrdemDeServico.EM_DIAGNOSTICO, new OrdemDeServicoEmDiagnosticoState());
    }
}
