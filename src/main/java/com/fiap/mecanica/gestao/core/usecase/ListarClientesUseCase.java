package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.dto.ListarClientesDto;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.shared.page.Pagina;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListarClientesUseCase {

	private final ClienteGateway clienteGateway;

	public Pagina<Cliente> listar(ListarClientesDto dto) {
		return clienteGateway.listar(dto.page(), dto.size());
	}
}
