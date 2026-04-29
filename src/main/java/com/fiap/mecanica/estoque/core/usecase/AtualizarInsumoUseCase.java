package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.dto.AtualizarInsumoDto;
import com.fiap.mecanica.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AtualizarInsumoUseCase {

	private final InsumoGateway insumoGateway;

	public void atualizar(AtualizarInsumoDto dto) {
		var insumo = insumoGateway.buscarPorId(dto.id()).orElseThrow(InsumoNaoEncontradoException::new);
		insumo.atualizar(dto.nome(), dto.descricao(), dto.preco(), dto.unidadeMedida(), dto.quantidadeEstoque());
		insumoGateway.atualizar(insumo);
	}
}
