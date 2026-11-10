package com.fiap.mecanica.ordemdeservico.infra.mensageria;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia do lado do consumidor: a OS tem UMA fila de respostas da saga ({@code ordemservico.respostas}), ligada ao
 * exchange {@code ordemservico.eventos} por uma routing key por tipo de resposta. Cobrança e estoque publicam no exchange
 * (declarado também por eles, com os mesmos argumentos). Mensagem que esgota as tentativas vai para a DLQ.
 */
@Configuration
@ConditionalOnProperty(name = "ordemservico.mensageria.listener.enabled", havingValue = "true", matchIfMissing = true)
public class OrdemServicoMensageriaConfig {

    /** Chave única com que o DLX encaminha toda mensagem morta para a DLQ, qualquer que tenha sido a original. */
    static final String CHAVE_DLQ = "respostas.dlq";

    @Bean
    DirectExchange ordemServicoEventosExchange(@Value("${ordemservico.mensageria.exchange}") String nome) {
        return new DirectExchange(nome, true, false);
    }

    @Bean
    DirectExchange ordemServicoEventosDeadLetterExchange(@Value("${ordemservico.mensageria.exchange}") String nome) {
        return new DirectExchange(nome + ".dlx", true, false);
    }

    @Bean
    Queue filaRespostas(@Value("${ordemservico.mensageria.fila.respostas}") String nome,
                        @Value("${ordemservico.mensageria.exchange}") String exchange) {
        return QueueBuilder.durable(nome)
                .deadLetterExchange(exchange + ".dlx")
                .deadLetterRoutingKey(CHAVE_DLQ)
                .build();
    }

    @Bean
    Queue filaRespostasDlq(@Value("${ordemservico.mensageria.fila.respostas}") String nome) {
        return QueueBuilder.durable(nome + ".dlq").build();
    }

    @Bean
    Binding bindingDlq(Queue filaRespostasDlq, DirectExchange ordemServicoEventosDeadLetterExchange) {
        return BindingBuilder.bind(filaRespostasDlq).to(ordemServicoEventosDeadLetterExchange).with(CHAVE_DLQ);
    }

    @Bean
    Binding bindingPagamentoConfirmado(Queue filaRespostas, DirectExchange ordemServicoEventosExchange,
                                       @Value("${ordemservico.mensageria.routing-key.pagamento-confirmado}") String chave) {
        return BindingBuilder.bind(filaRespostas).to(ordemServicoEventosExchange).with(chave);
    }

    @Bean
    Binding bindingPagamentoRecusado(Queue filaRespostas, DirectExchange ordemServicoEventosExchange,
                                     @Value("${ordemservico.mensageria.routing-key.pagamento-recusado}") String chave) {
        return BindingBuilder.bind(filaRespostas).to(ordemServicoEventosExchange).with(chave);
    }

    @Bean
    Binding bindingBaixaConfirmada(Queue filaRespostas, DirectExchange ordemServicoEventosExchange,
                                   @Value("${ordemservico.mensageria.routing-key.baixa-confirmada}") String chave) {
        return BindingBuilder.bind(filaRespostas).to(ordemServicoEventosExchange).with(chave);
    }

    @Bean
    Binding bindingReservaLiberada(Queue filaRespostas, DirectExchange ordemServicoEventosExchange,
                                   @Value("${ordemservico.mensageria.routing-key.reserva-liberada}") String chave) {
        return BindingBuilder.bind(filaRespostas).to(ordemServicoEventosExchange).with(chave);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
