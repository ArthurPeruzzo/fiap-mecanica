package com.fiap.mecanica.estoque.infra.gateway.database;

import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.estoque.infra.gateway.entity.PecaEntity;
import com.fiap.mecanica.estoque.infra.gateway.repository.PecaRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import com.fiap.mecanica.shared.page.Pagina;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

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

	@Override
	public Optional<Peca> buscarPorId(Long id) {
		try {
			return pecaRepository.findById(id)
					.map(e -> Peca.reconstituir(e.getId(), e.getNome(), e.getDescricao(), e.getPreco(), e.getQuantidadeEstoque()));
		} catch (Exception e) {
			log.error("Erro ao buscar peca por id: {}", id, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public void atualizar(Peca peca) {
		try {
			pecaRepository.save(PecaEntity.builder()
					.id(peca.getId())
					.nome(peca.getNome())
					.descricao(peca.getDescricao())
					.preco(peca.getPreco())
					.quantidadeEstoque(peca.getEstoqueTotal())
					.build());
		} catch (Exception e) {
			log.error("Erro ao atualizar peca", e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public void deletar(Long id) {
		try {
			pecaRepository.deleteById(id);
		} catch (Exception e) {
			log.error("Erro ao deletar peca por id: {}", id, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public Pagina<Peca> listar(int page, int size) {
		try {
			var resultado = pecaRepository.findAll(PageRequest.of(page, size));
			var pecas = resultado.getContent().stream()
					.map(e -> Peca.reconstituir(e.getId(), e.getNome(), e.getDescricao(), e.getPreco(), e.getQuantidadeEstoque()))
					.toList();
			return new Pagina<>(pecas, resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(), resultado.getTotalPages());
		} catch (Exception e) {
			log.error("Erro ao listar pecas", e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public List<Peca> listarPorIds(List<Long> pecasIds) {
		try {
			return pecaRepository.findAllById(pecasIds)
					.stream()
					.map(e -> Peca.reconstituir(e.getId(), e.getNome(), e.getDescricao(), e.getPreco(), e.getQuantidadeEstoque()))
					.toList();
		} catch (Exception e) {
			log.error("Erro ao listar pecas por ids: {}", pecasIds, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}
}
