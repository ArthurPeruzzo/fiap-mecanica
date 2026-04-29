package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico;

class OrdemDeServicoStateFactory {

    private OrdemDeServicoStateFactory() {}

    static OrdemDeServicoState from(StatusOrdemDeServico status) {
        return switch (status) {
            case RECEBIDA             -> new OrdemDeServicoRecebidaState();
            case EM_DIAGNOSTICO       -> new OrdemDeServicoEmDiagnosticoState();
            case DIAGNOSTICO_CONCLUIDO -> new OrdemDeServicoDiagnosticoConcluidoState();
            case FINALIZADA           -> new OrdemDeServicoFinalizadaState();
            case ENTREGUE             -> new OrdemDeServicoEntregueState();
        };
    }
}
