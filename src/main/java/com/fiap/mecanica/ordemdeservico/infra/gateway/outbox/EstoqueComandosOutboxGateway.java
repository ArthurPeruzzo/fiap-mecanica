package com.fiap.mecanica.ordemdeservico.infra.gateway.outbox;

import com.fiap.mecanica.ordemdeservico.core.gateway.EstoqueComandosGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Comandos ao fiap-estoque, entregues pela outbox ao exchange {@code estoque.comandos}. */
@Component
public class EstoqueComandosOutboxGateway implements EstoqueComandosGateway {

    static final String TIPO_LIBERAR_RESERVA = "LIBERAR_RESERVA";
    static final String TIPO_CONFIRMAR_BAIXA = "CONFIRMAR_BAIXA";

    private final OutboxRegistrador registrador;
    private final String exchange;
    private final String routingKeyLiberarReserva;
    private final String routingKeyConfirmarBaixa;

    public EstoqueComandosOutboxGateway(OutboxRegistrador registrador,
                                        @Value("${estoque.mensageria.exchange}") String exchange,
                                        @Value("${estoque.mensageria.routing-key.liberar-reserva}") String routingKeyLiberarReserva,
                                        @Value("${estoque.mensageria.routing-key.confirmar-baixa}") String routingKeyConfirmarBaixa) {
        this.registrador = registrador;
        this.exchange = exchange;
        this.routingKeyLiberarReserva = routingKeyLiberarReserva;
        this.routingKeyConfirmarBaixa = routingKeyConfirmarBaixa;
    }

    @Override
    public void solicitarLiberacaoDeReservas(Long ordemServicoId) {
        registrador.registrar(TIPO_LIBERAR_RESERVA, exchange, routingKeyLiberarReserva, ordemServicoId,
                "{\"ordemServicoId\":" + ordemServicoId + "}");
    }

    @Override
    public void solicitarConfirmacaoDeBaixa(Long ordemServicoId) {
        registrador.registrar(TIPO_CONFIRMAR_BAIXA, exchange, routingKeyConfirmarBaixa, ordemServicoId,
                "{\"ordemServicoId\":" + ordemServicoId + "}");
    }
}
