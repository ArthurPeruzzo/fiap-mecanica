package com.fiap.mecanica.ordemdeservico.infra.mensageria;

import com.fiap.mecanica.ordemdeservico.core.gateway.EstoqueComandosGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.TransacaoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.BaixaConfirmadaOrdemDeServicoUseCase;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.PagamentoConfirmadoOrdemDeServicoUseCase;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.PagamentoRecusadoOrdemDeServicoUseCase;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Orquestrador da saga: consome as respostas da cobrança e do estoque na fila única {@code ordemservico.respostas} e
 * decide o próximo passo pela routing key da mensagem. A entrega é "pelo menos uma vez": todos os casos de uso são
 * idempotentes (mensagem repetida com a OS já em outro status é confirmada sem efeito). Falha técnica propaga e aciona o
 * retry do listener; esgotadas as tentativas, a mensagem vai para a DLQ.
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "ordemservico.mensageria.listener.enabled", havingValue = "true", matchIfMissing = true)
public class RespostasDaSagaListener {

    private final PagamentoConfirmadoOrdemDeServicoUseCase pagamentoConfirmado;
    private final PagamentoRecusadoOrdemDeServicoUseCase pagamentoRecusado;
    private final BaixaConfirmadaOrdemDeServicoUseCase baixaConfirmada;

    private final String chavePagamentoConfirmado;
    private final String chavePagamentoRecusado;
    private final String chaveBaixaConfirmada;
    private final String chaveReservaLiberada;

    public RespostasDaSagaListener(OrdemDeServicoGateway ordemDeServicoGateway,
                                   EstoqueComandosGateway estoqueComandosGateway,
                                   TransacaoGateway transacaoGateway,
                                   NotificacaoGateway notificacaoGateway,
                                   @Value("${ordemservico.mensageria.routing-key.pagamento-confirmado}") String chavePagamentoConfirmado,
                                   @Value("${ordemservico.mensageria.routing-key.pagamento-recusado}") String chavePagamentoRecusado,
                                   @Value("${ordemservico.mensageria.routing-key.baixa-confirmada}") String chaveBaixaConfirmada,
                                   @Value("${ordemservico.mensageria.routing-key.reserva-liberada}") String chaveReservaLiberada) {
        this.pagamentoConfirmado = new PagamentoConfirmadoOrdemDeServicoUseCase(ordemDeServicoGateway, estoqueComandosGateway);
        this.pagamentoRecusado = new PagamentoRecusadoOrdemDeServicoUseCase(ordemDeServicoGateway, estoqueComandosGateway,
                transacaoGateway, notificacaoGateway);
        this.baixaConfirmada = new BaixaConfirmadaOrdemDeServicoUseCase(ordemDeServicoGateway);
        this.chavePagamentoConfirmado = chavePagamentoConfirmado;
        this.chavePagamentoRecusado = chavePagamentoRecusado;
        this.chaveBaixaConfirmada = chaveBaixaConfirmada;
        this.chaveReservaLiberada = chaveReservaLiberada;
    }

    @RabbitListener(queues = "${ordemservico.mensageria.fila.respostas}")
    public void receber(RespostaDaSagaMensagem resposta, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String chave) {
        if (resposta == null || resposta.ordemServicoId() == null) {
            // Mensagem malformada nunca vai passar numa nova tentativa: segue direto para a DLQ.
            throw new AmqpRejectAndDontRequeueException("Resposta da saga sem ordemServicoId (chave " + chave + ")");
        }
        Long id = resposta.ordemServicoId();
        if (chavePagamentoConfirmado.equals(chave)) {
            registrar("PagamentoConfirmado", id, pagamentoConfirmado.executar(id));
        } else if (chavePagamentoRecusado.equals(chave)) {
            registrar("PagamentoRecusado", id, pagamentoRecusado.executar(id));
        } else if (chaveBaixaConfirmada.equals(chave)) {
            registrar("BaixaConfirmada", id, baixaConfirmada.executar(id));
        } else if (chaveReservaLiberada.equals(chave)) {
            // Fim da compensação: o cancelamento já foi gravado e o cliente notificado quando a OS foi cancelada.
            log.info("Saga da ordem de servico {} encerrada: reservas liberadas pelo estoque", id);
        } else {
            throw new AmqpRejectAndDontRequeueException("Routing key desconhecida na fila de respostas: " + chave);
        }
    }

    private void registrar(String resposta, Long ordemServicoId, boolean aplicada) {
        if (aplicada) {
            log.info("Saga da ordem de servico {}: {} processada", ordemServicoId, resposta);
        } else {
            log.info("Saga da ordem de servico {}: {} ignorada (a OS ja nao esta aguardando pagamento)", ordemServicoId, resposta);
        }
    }
}
