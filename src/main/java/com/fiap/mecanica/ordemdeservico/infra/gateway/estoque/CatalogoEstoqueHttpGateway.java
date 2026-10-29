package com.fiap.mecanica.ordemdeservico.infra.gateway.estoque;

import com.fiap.mecanica.ordemdeservico.core.dto.ItemCatalogo;
import com.fiap.mecanica.ordemdeservico.core.exception.EstoqueIndisponivelException;
import com.fiap.mecanica.ordemdeservico.core.gateway.CatalogoEstoqueGateway;
import com.fiap.mecanica.ordemdeservico.infra.gateway.estoque.json.ItemCatalogoEstoqueJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

/**
 * Consulta o catálogo do fiap-estoque em lote. Ids que o estoque não conhece não voltam na resposta;
 * quem chama decide o que fazer com a diferença. Qualquer falha de comunicação vira
 * {@link EstoqueIndisponivelException}, pois sem o catálogo não há como validar nem precificar o item.
 */
@Component
@Slf4j
public class CatalogoEstoqueHttpGateway implements CatalogoEstoqueGateway {

	private static final String PECAS = "/catalogo/pecas";
	private static final String INSUMOS = "/catalogo/insumos";

	private final RestClient restClient;

	public CatalogoEstoqueHttpGateway(@Qualifier("estoqueRestClient") RestClient restClient) {
		this.restClient = restClient;
	}

	@Override
	public Optional<ItemCatalogo> buscarPeca(Long pecaId) {
		return consultar(PECAS, List.of(pecaId)).stream().findFirst();
	}

	@Override
	public Optional<ItemCatalogo> buscarInsumo(Long insumoId) {
		return consultar(INSUMOS, List.of(insumoId)).stream().findFirst();
	}

	@Override
	public List<ItemCatalogo> listarPecasPorIds(List<Long> pecasIds) {
		return consultar(PECAS, pecasIds);
	}

	@Override
	public List<ItemCatalogo> listarInsumosPorIds(List<Long> insumosIds) {
		return consultar(INSUMOS, insumosIds);
	}

	private List<ItemCatalogo> consultar(String caminho, List<Long> ids) {
		if (ids == null || ids.isEmpty()) return List.of();

		try {
			List<ItemCatalogoEstoqueJson> itens = restClient.get()
					.uri(uri -> uri.path(caminho).queryParam("ids", ids).build())
					.retrieve()
					.body(new ParameterizedTypeReference<>() {
					});
			return itens == null ? List.of() : itens.stream()
					.map(item -> new ItemCatalogo(item.id(), item.preco()))
					.toList();
		} catch (RestClientException e) {
			log.error("Falha ao consultar o catálogo do estoque em '{}' para os ids {}", caminho, ids, e);
			throw new EstoqueIndisponivelException();
		}
	}
}
