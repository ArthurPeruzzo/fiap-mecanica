package com.fiap.mecanica.ordemdeservico.core.gateway;

/**
 * Porta da ordem de serviço para o estoque. Hoje chama o módulo de estoque em processo;
 * ao extrair o microsserviço, a implementação passa a ser HTTP/mensageria.
 */
public interface ReservaEstoqueGateway {
    void reservarPeca(Long ordemServicoId, Long pecaId, Integer quantidadeTotal);
    void reservarInsumo(Long ordemServicoId, Long insumoId, Integer quantidadeTotal);
    void confirmarReservas(Long ordemServicoId);
    void liberarReservas(Long ordemServicoId);
}
