package com.fiap.mecanica.ordemdeservico.infra.gateway.estoque;

import com.fiap.mecanica.ordemdeservico.core.dto.ItemCatalogo;
import com.fiap.mecanica.ordemdeservico.core.exception.EstoqueIndisponivelException;
import com.fiap.mecanica.shared.http.RestClientFactory;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercita o RestClient real contra o WireMock: rota, parâmetros, mapeamento do JSON e falhas.
 */
class CatalogoEstoqueHttpGatewayUnitTest {

    @RegisterExtension
    static WireMockExtension estoque = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private CatalogoEstoqueHttpGateway gateway;

    @BeforeEach
    void setUp() {
        var restClient = new RestClientFactory().criar(estoque.baseUrl(), Duration.ofSeconds(2), Duration.ofSeconds(1));
        gateway = new CatalogoEstoqueHttpGateway(restClient);
    }

    @Test
    void listarPecasPorIds_shouldConsultarEmUmaUnicaChamadaEMapearIdEPreco() {
        estoque.stubFor(get(urlPathEqualTo("/catalogo/pecas"))
                .withQueryParam("ids", equalTo("5"))
                .withQueryParam("ids", equalTo("6"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":5,\"preco\":42.00},{\"id\":6,\"preco\":89.90}]")));

        var itens = gateway.listarPecasPorIds(List.of(5L, 6L));

        assertEquals(List.of(new ItemCatalogo(5L, new BigDecimal("42.00")),
                new ItemCatalogo(6L, new BigDecimal("89.90"))), itens);
        estoque.verify(exactly(1), getRequestedFor(urlPathEqualTo("/catalogo/pecas")));
    }

    @Test
    void listarInsumosPorIds_shouldConsultarRotaDeInsumos() {
        estoque.stubFor(get(urlPathEqualTo("/catalogo/insumos"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":3,\"preco\":38.00}]")));

        var itens = gateway.listarInsumosPorIds(List.of(3L));

        assertEquals(List.of(new ItemCatalogo(3L, new BigDecimal("38.00"))), itens);
    }

    @Test
    void buscarPeca_shouldRetornarItemQuandoExiste() {
        estoque.stubFor(get(urlPathEqualTo("/catalogo/pecas")).withQueryParam("ids", equalTo("5"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":5,\"preco\":42.00}]")));

        assertEquals(Optional.of(new ItemCatalogo(5L, new BigDecimal("42.00"))), gateway.buscarPeca(5L));
    }

    @Test
    void buscarPeca_shouldRetornarVazioQuandoEstoqueNaoConhece() {
        estoque.stubFor(get(urlPathEqualTo("/catalogo/pecas"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[]")));

        assertTrue(gateway.buscarPeca(999L).isEmpty());
    }

    @Test
    void buscarInsumo_shouldRetornarItemQuandoExiste() {
        estoque.stubFor(get(urlPathEqualTo("/catalogo/insumos")).withQueryParam("ids", equalTo("3"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":3,\"preco\":38.00}]")));

        assertEquals(Optional.of(new ItemCatalogo(3L, new BigDecimal("38.00"))), gateway.buscarInsumo(3L));
    }

    @Test
    void listarPorIds_shouldIgnorarListaVaziaSemChamarEstoque() {
        assertTrue(gateway.listarPecasPorIds(List.of()).isEmpty());
        assertTrue(gateway.listarInsumosPorIds(null).isEmpty());

        assertTrue(estoque.getAllServeEvents().isEmpty());
    }

    @Test
    void listarPecasPorIds_shouldLancarEstoqueIndisponivelQuandoEstoqueRetorna5xx() {
        estoque.stubFor(get(urlPathEqualTo("/catalogo/pecas")).willReturn(aResponse().withStatus(500)));

        assertThrows(EstoqueIndisponivelException.class, () -> gateway.listarPecasPorIds(List.of(5L)));
    }

    @Test
    void listarPecasPorIds_shouldLancarEstoqueIndisponivelQuandoEstoqueRetorna4xx() {
        estoque.stubFor(get(urlPathEqualTo("/catalogo/pecas")).willReturn(aResponse().withStatus(400)));

        assertThrows(EstoqueIndisponivelException.class, () -> gateway.listarPecasPorIds(List.of(5L)));
    }

    @Test
    void listarPecasPorIds_shouldLancarEstoqueIndisponivelQuandoDemoraMaisQueOTimeout() {
        estoque.stubFor(get(urlPathEqualTo("/catalogo/pecas"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[]").withFixedDelay(2500)));

        assertThrows(EstoqueIndisponivelException.class, () -> gateway.listarPecasPorIds(List.of(5L)));
    }

    @Test
    void listarPecasPorIds_shouldLancarEstoqueIndisponivelQuandoServidorForaDoAr() {
        var foraDoAr = new CatalogoEstoqueHttpGateway(
                new RestClientFactory().criar("http://127.0.0.1:1", Duration.ofMillis(300), Duration.ofMillis(300)));

        assertThrows(EstoqueIndisponivelException.class, () -> foraDoAr.listarPecasPorIds(List.of(5L)));
    }
}
