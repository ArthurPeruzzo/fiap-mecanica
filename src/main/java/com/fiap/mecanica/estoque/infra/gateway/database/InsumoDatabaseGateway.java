package com.fiap.mecanica.estoque.infra.gateway.database;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.infra.gateway.entity.InsumoEntity;
import com.fiap.mecanica.estoque.infra.gateway.repository.InsumoRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import com.fiap.mecanica.shared.page.Pagina;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

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

	@Override
	public Optional<Insumo> buscarPorId(Long id) {
		try {
			return insumoRepository.findById(id)
					.map(e -> Insumo.reconstituir(e.getId(), e.getNome(), e.getDescricao(), e.getPreco(), e.getUnidadeMedida(), e.getQuantidadeEstoque()));
		} catch (Exception e) {
			log.error("Erro ao buscar insumo por id: {}", id, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public void atualizar(Insumo insumo) {
		try {
			insumoRepository.save(InsumoEntity.builder()
					.id(insumo.getId())
					.nome(insumo.getNome())
					.descricao(insumo.getDescricao())
					.preco(insumo.getPreco())
					.quantidadeEstoque(insumo.getEstoqueTotal())
					.unidadeMedida(insumo.getUnidadeMedida())
					.build());
		} catch (Exception e) {
			log.error("Erro ao atualizar insumo", e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public void deletar(Long id) {
		try {
			insumoRepository.deleteById(id);
		} catch (Exception e) {
			log.error("Erro ao deletar insumo por id: {}", id, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public Pagina<Insumo> listar(int page, int size) {
		try {
			var resultado = insumoRepository.findAll(PageRequest.of(page, size));
			var insumos = resultado.getContent().stream()
					.map(e -> Insumo.reconstituir(e.getId(), e.getNome(), e.getDescricao(), e.getPreco(), e.getUnidadeMedida(), e.getQuantidadeEstoque()))
					.toList();
			return new Pagina<>(insumos, resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(), resultado.getTotalPages());
		} catch (Exception e) {
			log.error("Erro ao listar insumos", e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}
}
