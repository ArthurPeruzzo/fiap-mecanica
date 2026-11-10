package com.fiap.mecanica.ordemdeservico.infra.gateway.outbox;

import com.fiap.mecanica.ordemdeservico.core.gateway.PagamentoComandosGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Comandos ao fiap-cobranca, entregues pela outbox ao exchange {@code cobranca.comandos}. */
@Component
public class PagamentoComandosOutboxGateway implements PagamentoComandosGateway {

    static final String TIPO_SOLICITAR_PAGAMENTO = "SOLICITAR_PAGAMENTO";

    private final OutboxRegistrador registrador;
    private final String exchange;
    private final String routingKeySolicitarPagamento;

    public PagamentoComandosOutboxGateway(OutboxRegistrador registrador,
                                          @Value("${cobranca.mensageria.exchange}") String exchange,
                                          @Value("${cobranca.mensageria.routing-key.solicitar-pagamento}") String routingKeySolicitarPagamento) {
        this.registrador = registrador;
        this.exchange = exchange;
        this.routingKeySolicitarPagamento = routingKeySolicitarPagamento;
    }

    @Override
    public void solicitarPagamento(Long ordemServicoId, Long clienteId, BigDecimal valor) {
        // O valor vai como número (BigDecimal.toPlainString evita notação científica) para o consumidor ler como decimal.
        registrador.registrar(TIPO_SOLICITAR_PAGAMENTO, exchange, routingKeySolicitarPagamento, ordemServicoId,
                "{\"ordemServicoId\":" + ordemServicoId + ",\"clienteId\":" + clienteId
                        + ",\"valor\":" + valor.toPlainString() + "}");
    }
}
