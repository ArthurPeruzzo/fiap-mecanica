package com.fiap.mecanica.ordemdeservico.infra.gateway.outbox;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Topologia do lado do produtor: só os exchanges de comandos dos outros serviços. As filas (e as DLQs) pertencem aos
 * consumidores (fiap-estoque e fiap-cobranca). Cada exchange é declarado nos dois lados com os mesmos argumentos, então
 * qual subir primeiro não importa.
 */
@Configuration
@EnableScheduling
public class MensageriaEstoqueConfig {

    @Bean
    DirectExchange estoqueComandosExchange(@Value("${estoque.mensageria.exchange}") String nome) {
        return new DirectExchange(nome, true, false);
    }

    @Bean
    DirectExchange cobrancaComandosExchange(@Value("${cobranca.mensageria.exchange}") String nome) {
        return new DirectExchange(nome, true, false);
    }
}
