package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.dto.CriarInsumoDto;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CriarInsumoUseCase {

	private final InsumoGateway insumoGateway;

	public void criar(CriarInsumoDto dto) {
		Insumo insumo = new Insumo(dto.nome(), dto.descricao(), dto.preco(), dto.unidadeMedida(), dto.quantidadeEstoque());
		insumoGateway.criar(insumo);
	}
}
