package com.fiap.mecanica.ordemdeservico.core.gateway;

import com.fiap.mecanica.ordemdeservico.core.dto.ItemReserva;

import java.util.List;

/**
 * Porta da ordem de serviço para o serviço de estoque (fiap-estoque), via REST.
 */
public interface ReservaEstoqueGateway {
    /**
     * Define, numa única chamada, a quantidade total reservada de cada item. Idempotente e atômico no
     * estoque: se algum item falhar, nenhum é reservado.
     *
     * @throws com.fiap.mecanica.ordemdeservico.core.exception.EstoqueInsuficienteParaOrdemException se faltar saldo
     * @throws com.fiap.mecanica.ordemdeservico.core.exception.EstoqueIndisponivelException         se o estoque não responder
     */
    void reservarItens(Long ordemServicoId, List<ItemReserva> itens);
    void confirmarReservas(Long ordemServicoId);
    void liberarReservas(Long ordemServicoId);
}
