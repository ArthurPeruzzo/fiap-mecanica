package com.fiap.mecanica.estoque.infra.gateway.database;

import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.estoque.infra.gateway.entity.PecaEntity;
import com.fiap.mecanica.estoque.infra.gateway.repository.PecaRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PecaDatabaseGateway implements PecaGateway {

	private final PecaRepository pecaRepository;

	@Override
	public void criar(Peca peca) {
		try {
			pecaRepository.save(PecaEntity.builder()
							.nome(peca.getNome())
							.descricao(peca.getDescricao())
							.preco(peca.getPreco())
							.quantidadeEstoque(peca.getEstoqueTotal())
					.build());
		} catch (Exception e) {
			log.error("Erro ao criar peca", e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}
}
