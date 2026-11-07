package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico;

public enum StatusOrdemDeServico {
    RECEBIDA,
    EM_DIAGNOSTICO,
    DIAGNOSTICO_CONCLUIDO,
    AGUARDANDO_APROVACAO,
    AGUARDANDO_PAGAMENTO,
    CANCELADA,
    EM_EXECUCAO,
    FINALIZADA,
    ENTREGUE
}
