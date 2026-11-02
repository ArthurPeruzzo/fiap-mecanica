package com.fiap.mecanica.ordemdeservico.infra.gateway.estoque.json;

import java.util.List;

public record ReservarItensEstoqueJson(List<ItemJson> itens) {

	public record ItemJson(String tipoItem, Long itemId, Integer quantidadeTotal) {
	}
}
