package com.fiap.mecanica.estoque.infra.gateway.database;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.domain.ItemEstoque;
import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.domain.TipoItemReserva;
import com.fiap.mecanica.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.mecanica.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.ItemEstoqueGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ItemEstoqueDatabaseGateway implements ItemEstoqueGateway {

	private final PecaGateway pecaGateway;
	private final InsumoGateway insumoGateway;

	@Override
	public ItemEstoque buscar(TipoItemReserva tipoItem, Long itemId) {
		return switch (tipoItem) {
			case PECA -> pecaGateway.buscarPorId(itemId).orElseThrow(PecaNaoEncontradaException::new);
			case INSUMO -> insumoGateway.buscarPorId(itemId).orElseThrow(InsumoNaoEncontradoException::new);
		};
	}

	@Override
	public void atualizar(ItemEstoque item) {
		switch (item) {
			case Peca peca -> pecaGateway.atualizar(peca);
			case Insumo insumo -> insumoGateway.atualizar(insumo);
			default -> throw new IllegalArgumentException("Tipo de item de estoque desconhecido");
		}
	}
}
