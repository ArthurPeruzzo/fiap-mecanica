package com.fiap.mecanica.ordemdeservico.core.gateway;

/**
 * Comandos assíncronos enviados ao fiap-estoque. A chamada só registra a intenção, dentro da transação
 * em curso; a entrega ao broker acontece depois e é repetida até o estoque confirmar o recebimento.
 */
public interface EstoqueComandosGateway {

    /** Pede ao estoque que devolva ao saldo tudo que está reservado para a ordem de serviço. */
    void solicitarLiberacaoDeReservas(Long ordemServicoId);

    /** Pede ao estoque que transforme as reservas da ordem de serviço em baixa definitiva (pagamento aprovado). */
    void solicitarConfirmacaoDeBaixa(Long ordemServicoId);
}
