package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.dto.ListarPecasDto;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.shared.page.Pagina;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListarPecasUseCase {

	private final PecaGateway pecaGateway;

	public Pagina<Peca> listar(ListarPecasDto dto) {
		return pecaGateway.listar(dto.page(), dto.size());
	}
}
