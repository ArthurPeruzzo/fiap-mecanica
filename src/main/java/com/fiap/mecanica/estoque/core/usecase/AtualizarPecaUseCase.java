package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.dto.AtualizarPecaDto;
import com.fiap.mecanica.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AtualizarPecaUseCase {

	private final PecaGateway pecaGateway;

	public void atualizar(AtualizarPecaDto dto) {
		var peca = pecaGateway.buscarPorId(dto.id()).orElseThrow(PecaNaoEncontradaException::new);
		peca.atualizar(dto.nome(), dto.descricao(), dto.preco(), dto.quantidadeEstoque());
		pecaGateway.atualizar(peca);
	}
}
