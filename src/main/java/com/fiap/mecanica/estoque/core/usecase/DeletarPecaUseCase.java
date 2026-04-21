package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeletarPecaUseCase {

	private final PecaGateway pecaGateway;

	public void deletar(Long id) {
		pecaGateway.buscarPorId(id).orElseThrow(PecaNaoEncontradaException::new);
		pecaGateway.deletar(id);
	}
}
