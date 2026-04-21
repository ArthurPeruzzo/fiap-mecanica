package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeletarClienteUseCase {

	private final ClienteGateway clienteGateway;

	public void deletar(Long id) {
		clienteGateway.buscarPorId(id)
				.orElseThrow(ClienteNaoEncontradoException::new);

		clienteGateway.deletar(id);
	}
}
