package com.fiap.mecanica.ordemdeservico.core.domain;

class OrdemDeServicoStateFactory {

    private OrdemDeServicoStateFactory() {}

    static OrdemDeServicoState from(StatusOrdemDeServico status) {
        return switch (status) {
            case RECEBIDA -> new OrdemDeServicoRecebidaState();
            case EM_DIAGNOSTICO -> new OrdemDeServicoEmDiagnosticoState();
            case DIAGNOSTICO_CONCLUIDO -> new OrdemDeServicoDiagnosticoConcluidoState();
        };
    }
}
