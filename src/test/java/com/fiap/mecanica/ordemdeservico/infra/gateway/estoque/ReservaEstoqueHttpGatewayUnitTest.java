package com.fiap.mecanica.ordemdeservico.infra.gateway.estoque;

import com.fiap.mecanica.ordemdeservico.core.dto.ItemReserva;
import com.fiap.mecanica.ordemdeservico.core.exception.EstoqueIndisponivelException;
import com.fiap.mecanica.ordemdeservico.core.exception.EstoqueInsuficienteParaOrdemException;
import com.fiap.mecanica.shared.http.RestClientFactory;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercita o RestClient real (RestClientFactory) contra um servidor HTTP local, para validar
 * rota, corpo, autenticação, mapeamento de erros e timeout sem depender do fiap-estoque.
 */
class ReservaEstoqueHttpGatewayUnitTest {


    private HttpServer servidor;
    private final List<Requisicao> requisicoes = new CopyOnWriteArrayList<>();
    private volatile int statusResposta = 204;
    private volatile long atrasoMs = 0;

    private record Requisicao(String metodo, String caminho, String corpo) {
    }

    @BeforeEach
    void subirServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", exchange -> {
            String corpo = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            requisicoes.add(new Requisicao(exchange.getRequestMethod(), exchange.getRequestURI().getPath(),
                    corpo));
            if (atrasoMs > 0) {
                try {
                    Thread.sleep(atrasoMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            exchange.sendResponseHeaders(statusResposta, -1);
            exchange.close();
        });
        servidor.start();
    }

    @AfterEach
    void pararServidor() {
        servidor.stop(0);
    }

    private ReservaEstoqueHttpGateway gateway() {
        return gatewayApontandoPara(servidor.getAddress().getPort());
    }

    private ReservaEstoqueHttpGateway gatewayApontandoPara(int porta) {
        var restClient = new RestClientFactory().criar(
                "http://127.0.0.1:" + porta, Duration.ofMillis(500), Duration.ofMillis(300));
        return new ReservaEstoqueHttpGateway(restClient);
    }

    @Test
    void reservarItens_shouldEnviarLoteNumaUnicaRequisicaoPut() {
        gateway().reservarItens(7L, List.of(ItemReserva.peca(5L, 3), ItemReserva.insumo(30L, 4)));

        assertEquals(1, requisicoes.size());
        var req = requisicoes.getFirst();
        assertEquals("PUT", req.metodo());
        assertEquals("/reserva/ordem-servico/7/itens", req.caminho());
        assertEquals("""
                {"itens":[{"tipoItem":"PECA","itemId":5,"quantidadeTotal":3},{"tipoItem":"INSUMO","itemId":30,"quantidadeTotal":4}]}""",
                req.corpo());
    }

    @Test
    void reservarItens_shouldIgnorarListaVazia() {
        gateway().reservarItens(7L, List.of());

        assertTrue(requisicoes.isEmpty());
    }

    @Test
    void reservarItens_shouldTraduzir422ParaEstoqueInsuficiente() {
        statusResposta = 422;
        var gateway = gateway();
        var itens = List.of(ItemReserva.peca(5L, 3));

        assertThrows(EstoqueInsuficienteParaOrdemException.class, () -> gateway.reservarItens(7L, itens));
    }

    @Test
    void reservarItens_shouldTraduzirOutros4xxParaEstoqueIndisponivel() {
        statusResposta = 404;
        var gateway = gateway();
        var itens = List.of(ItemReserva.peca(5L, 3));

        assertThrows(EstoqueIndisponivelException.class, () -> gateway.reservarItens(7L, itens));
    }

    @Test
    void reservarItens_shouldTraduzir5xxParaEstoqueIndisponivel() {
        statusResposta = 500;
        var gateway = gateway();
        var itens = List.of(ItemReserva.peca(5L, 3));

        assertThrows(EstoqueIndisponivelException.class, () -> gateway.reservarItens(7L, itens));
    }

    @Test
    void reservarItens_shouldFalharComEstoqueIndisponivelQuandoEstoqueEstaForaDoAr() {
        int porta = servidor.getAddress().getPort();
        servidor.stop(0);
        var gateway = gatewayApontandoPara(porta);
        var itens = List.of(ItemReserva.peca(5L, 3));

        assertThrows(EstoqueIndisponivelException.class, () -> gateway.reservarItens(7L, itens));
    }

    @Test
    void reservarItens_shouldFalharComEstoqueIndisponivelQuandoEstoourarOTimeoutDeLeitura() {
        atrasoMs = 1500;
        var gateway = gateway();
        var itens = List.of(ItemReserva.peca(5L, 3));

        assertThrows(EstoqueIndisponivelException.class, () -> gateway.reservarItens(7L, itens));
    }

    @Test
    void confirmarReservas_shouldChamarPostConfirmar() {
        gateway().confirmarReservas(7L);

        var req = requisicoes.getFirst();
        assertEquals("POST", req.metodo());
        assertEquals("/reserva/ordem-servico/7/confirmar", req.caminho());
    }

    @Test
    void liberarReservas_shouldChamarPostLiberar() {
        gateway().liberarReservas(7L);

        var req = requisicoes.getFirst();
        assertEquals("POST", req.metodo());
        assertEquals("/reserva/ordem-servico/7/liberar", req.caminho());
    }

    @Test
    void confirmarELiberar_shouldTraduzirFalhaParaEstoqueIndisponivel() {
        statusResposta = 503;
        var gateway = gateway();

        assertThrows(EstoqueIndisponivelException.class, () -> gateway.confirmarReservas(7L));
        assertThrows(EstoqueIndisponivelException.class, () -> gateway.liberarReservas(7L));
    }
}
