package com.fiap.mecanica.resources;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import io.restassured.path.json.JsonPath;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

/**
 * O fiap-estoque simulado (WireMock) usado pelos testes de integração e pelo BDD: respostas do catálogo e das reservas e
 * a conferência das chamadas que o monólito fez. O monólito não guarda peça nem insumo, então os testes só podem
 * afirmar o que ele PEDIU ao estoque; o efeito real no saldo é coberto no teste de ponta a ponta contra o ambiente.
 */
public class EstoqueSimulado {

    /** Ids do catálogo simulado. */
    public static final Long PECA_ID = 11L;
    public static final Long INSUMO_ID = 21L;
    public static final String RESERVAR_ITENS = "/reserva/ordem-servico/\\d+/itens";
    public static final String CONFIRMAR = "/reserva/ordem-servico/\\d+/confirmar";
    public static final String LIBERAR = "/reserva/ordem-servico/\\d+/liberar";

    private final WireMockServer estoque;

    public EstoqueSimulado(WireMockServer estoque) {
        this.estoque = estoque;
    }

    public void stubCatalogo() {
        estoque.stubFor(get(urlPathEqualTo("/catalogo/pecas"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":" + PECA_ID + ",\"preco\":45.00}]")));
        estoque.stubFor(get(urlPathEqualTo("/catalogo/insumos"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":" + INSUMO_ID + ",\"preco\":35.00}]")));
    }

    public void stubCatalogoVazio() {
        estoque.stubFor(get(urlPathEqualTo("/catalogo/pecas"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[]")));
        estoque.stubFor(get(urlPathEqualTo("/catalogo/insumos"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[]")));
    }

    public void stubReservaOk() {
        estoque.stubFor(put(urlPathMatching(RESERVAR_ITENS)).willReturn(aResponse().withStatus(204)));
    }

    public void stubReservaStatus(int status) {
        estoque.stubFor(put(urlPathMatching(RESERVAR_ITENS)).willReturn(aResponse().withStatus(status)));
    }

    public void stubConfirmarELiberarOk() {
        estoque.stubFor(post(urlPathMatching(CONFIRMAR)).willReturn(aResponse().withStatus(204)));
        estoque.stubFor(post(urlPathMatching(LIBERAR)).willReturn(aResponse().withStatus(204)));
    }

    /** O estoque responde com erro a qualquer chamada de reserva, confirmação ou liberação (fora do ar). */
    public void stubForaDoAr() {
        estoque.stubFor(put(urlPathMatching(RESERVAR_ITENS)).willReturn(aResponse().withStatus(503)));
        estoque.stubFor(post(urlPathMatching(CONFIRMAR)).willReturn(aResponse().withStatus(503)));
        estoque.stubFor(post(urlPathMatching(LIBERAR)).willReturn(aResponse().withStatus(503)));
    }

    /** A reserva é aceita, mas a resposta só chega depois do atraso (maior que o limite de espera do monólito). */
    public void stubReservaLenta(int atrasoEmMilissegundos) {
        estoque.stubFor(put(urlPathMatching(RESERVAR_ITENS))
                .willReturn(aResponse().withStatus(204).withFixedDelay(atrasoEmMilissegundos)));
    }

    public void stubCatalogoPeca(Long id, String preco) {
        estoque.stubFor(get(urlPathEqualTo("/catalogo/pecas")).withQueryParam("ids", equalTo(String.valueOf(id)))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":" + id + ",\"preco\":" + preco + "}]")));
    }

    public void stubCatalogoInsumo(Long id, String preco) {
        estoque.stubFor(get(urlPathEqualTo("/catalogo/insumos")).withQueryParam("ids", equalTo(String.valueOf(id)))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":" + id + ",\"preco\":" + preco + "}]")));
    }

    /** As quantidadeTotal enviadas ao estoque nas reservas, na ordem em que foram feitas. */
    public List<Integer> quantidadesReservadas() {
        Pattern quantidade = Pattern.compile("\"quantidadeTotal\"\\s*:\\s*(\\d+)");
        return estoque.findAll(putRequestedFor(urlPathMatching(RESERVAR_ITENS))).stream()
                .map(LoggedRequest::getBodyAsString)
                .map(quantidade::matcher)
                .filter(Matcher::find)
                .map(m -> Integer.valueOf(m.group(1)))
                .toList();
    }

    /** Itens ({@code tipoItem}, {@code itemId}, {@code quantidadeTotal}) do ÚLTIMO pedido de reserva da OS. */
    public List<Map<String, Object>> itensDaUltimaReserva(Long ordemServicoId) {
        List<LoggedRequest> pedidos = estoque.findAll(
                putRequestedFor(urlEqualTo("/reserva/ordem-servico/" + ordemServicoId + "/itens")));
        if (pedidos.isEmpty()) {
            return List.of();
        }
        return JsonPath.from(pedidos.getLast().getBodyAsString()).getList("itens");
    }

    public int pedidosDeConfirmacao(Long ordemServicoId) {
        return estoque.findAll(postRequestedFor(urlEqualTo("/reserva/ordem-servico/" + ordemServicoId + "/confirmar"))).size();
    }

    /** Quantas confirmações de baixa da OS o estoque ACEITOU (resposta 204); as recusadas não contam. */
    public long confirmacoesBemSucedidas(Long ordemServicoId) {
        String url = "/reserva/ordem-servico/" + ordemServicoId + "/confirmar";
        return estoque.getAllServeEvents().stream()
                .filter(evento -> evento.getRequest().getUrl().equals(url))
                .filter(evento -> evento.getResponse().getStatus() == 204)
                .count();
    }

    /** Pedidos de liberação de qualquer OS (útil quando a OS nem chegou a ser gravada e não há id). */
    public int totalPedidosDeLiberacao() {
        return estoque.findAll(postRequestedFor(urlPathMatching(LIBERAR))).size();
    }

    public int pedidosDeLiberacao(Long ordemServicoId) {
        return estoque.findAll(postRequestedFor(urlEqualTo("/reserva/ordem-servico/" + ordemServicoId + "/liberar"))).size();
    }
}
