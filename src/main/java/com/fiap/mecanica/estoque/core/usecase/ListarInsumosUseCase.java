package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.dto.ListarInsumosDto;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.shared.page.Pagina;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListarInsumosUseCase {

	private final InsumoGateway insumoGateway;

	public Pagina<Insumo> listar(ListarInsumosDto dto) {
		return insumoGateway.listar(dto.page(), dto.size());
	}
}
