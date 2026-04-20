package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.dto.AtualizarClienteDto;
import com.fiap.mecanica.gestao.core.exception.ClienteJaExisteException;
import com.fiap.mecanica.gestao.core.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AtualizarClienteUseCase {

	private final ClienteGateway clienteGateway;

	public void atualizar(AtualizarClienteDto dto) {
		var cliente = clienteGateway.buscarPorId(dto.id())
				.orElseThrow(ClienteNaoEncontradoException::new);

		cliente.atualizar(new NomeCompleto(dto.nome(), dto.sobrenome()), dto.cnpj(), dto.cpf());

		cliente.getCpf().ifPresent(cpf -> {
			if (clienteGateway.existePorCpfExcluindoId(cpf.getValor(), dto.id())) {
				throw new ClienteJaExisteException();
			}
		});

		cliente.getCnpj().ifPresent(cnpj -> {
			if (clienteGateway.existePorCnpjExcluindoId(cnpj.getValor(), dto.id())) {
				throw new ClienteJaExisteException();
			}
		});

		clienteGateway.atualizar(cliente);
	}
}
