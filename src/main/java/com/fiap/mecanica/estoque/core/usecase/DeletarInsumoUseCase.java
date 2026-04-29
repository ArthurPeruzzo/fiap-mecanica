package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeletarInsumoUseCase {

	private final InsumoGateway insumoGateway;

	public void deletar(Long id) {
		insumoGateway.buscarPorId(id).orElseThrow(InsumoNaoEncontradoException::new);
		insumoGateway.deletar(id);
	}
}
