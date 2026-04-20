package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.dto.CriarClienteDto;
import com.fiap.mecanica.gestao.core.exception.ClienteJaExisteException;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CriarClienteUseCase {

	private final ClienteGateway clienteGateway;

	public void criar(CriarClienteDto dto) {
		var cliente = new Cliente(new NomeCompleto(dto.nome(), dto.sobrenome()), dto.cnpj(), dto.cpf());

		cliente.getCpf().ifPresent(cpf -> {
			if (clienteGateway.existePorCpf(cpf.getValor())) {
				throw new ClienteJaExisteException();
			}
		});

		cliente.getCnpj().ifPresent(cnpj -> {
			if (clienteGateway.existePorCnpj(cnpj.getValor())) {
				throw new ClienteJaExisteException();
			}
		});

		clienteGateway.criar(cliente);
	}
}
