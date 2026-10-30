package com.fiap.mecanica.ordemdeservico.core.gateway;

/**
 * Limite de transação do banco local. Quem orquestra o caso de uso decide o que entra na
 * transação e o que compensar se ela falhar.
 */
public interface TransacaoGateway {
    void executar(Runnable acao);
}
