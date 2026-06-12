package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico;

public class OrdemDeServicoStateFactory {

    private OrdemDeServicoStateFactory() {}

    public static OrdemDeServicoState from(StatusOrdemDeServico status) {
        return switch (status) {
            case RECEBIDA             -> new OrdemDeServicoRecebidaState();
            case EM_DIAGNOSTICO       -> new OrdemDeServicoEmDiagnosticoState();
            case DIAGNOSTICO_CONCLUIDO -> new OrdemDeServicoDiagnosticoConcluidoState();
            case AGUARDANDO_APROVACAO -> new OrdemDeServicoAguardandoAprovacaoState();
            case CANCELADA            -> new OrdemDeServicoCanceladaState();
            case EM_EXECUCAO          -> new OrdemDeServicoEmExecucaoState();
            case FINALIZADA           -> new OrdemDeServicoFinalizadaState();
            case ENTREGUE             -> new OrdemDeServicoEntregueState();
        };
    }
}
