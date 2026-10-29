package com.fiap.mecanica.resources.estoque;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.ordemdeservico.core.dto.ItemCatalogo;
import com.fiap.mecanica.ordemdeservico.core.gateway.CatalogoEstoqueGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Substitui o cliente HTTP do catálogo nos testes de integração que ainda cadastram peças e insumos
 * no módulo de estoque embutido. Testes com estoque.fake=false consultam o fiap-estoque (WireMock).
 */
@Component
@Primary
@Profile("integration-test")
@ConditionalOnProperty(name = "estoque.fake", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class CatalogoEstoqueLocalFake implements CatalogoEstoqueGateway {

	private final PecaGateway pecaGateway;
	private final InsumoGateway insumoGateway;

	@Override
	public Optional<ItemCatalogo> buscarPeca(Long pecaId) {
		return pecaGateway.buscarPorId(pecaId).map(this::toItemCatalogo);
	}

	@Override
	public Optional<ItemCatalogo> buscarInsumo(Long insumoId) {
		return insumoGateway.buscarPorId(insumoId).map(this::toItemCatalogo);
	}

	@Override
	public List<ItemCatalogo> listarPecasPorIds(List<Long> pecasIds) {
		return pecaGateway.listarPorIds(pecasIds).stream().map(this::toItemCatalogo).toList();
	}

	@Override
	public List<ItemCatalogo> listarInsumosPorIds(List<Long> insumosIds) {
		return insumoGateway.listarPorIds(insumosIds).stream().map(this::toItemCatalogo).toList();
	}

	private ItemCatalogo toItemCatalogo(Peca peca) {
		return new ItemCatalogo(peca.getId(), peca.getPreco());
	}

	private ItemCatalogo toItemCatalogo(Insumo insumo) {
		return new ItemCatalogo(insumo.getId(), insumo.getPreco());
	}
}
