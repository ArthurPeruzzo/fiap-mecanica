package com.fiap.mecanica.estoque.infra.gateway.database;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.infra.gateway.entity.InsumoEntity;
import com.fiap.mecanica.estoque.infra.gateway.repository.InsumoRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InsumoDatabaseGateway implements InsumoGateway {

	private final InsumoRepository insumoRepository;

	@Override
	public void criar(Insumo insumo) {
		try {
			insumoRepository.save(InsumoEntity.builder()
							.nome(insumo.getNome())
							.descricao(insumo.getDescricao())
							.preco(insumo.getPreco())
							.quantidadeEstoque(insumo.getEstoqueTotal())
							.unidadeMedida(insumo.getUnidadeMedida())
					.build());
		} catch (Exception e) {
			log.error("Erro ao criar insumo", e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}
}
