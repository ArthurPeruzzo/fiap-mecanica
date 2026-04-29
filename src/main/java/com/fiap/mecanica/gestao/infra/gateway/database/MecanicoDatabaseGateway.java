package com.fiap.mecanica.gestao.infra.gateway.database;

import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import com.fiap.mecanica.gestao.core.domain.Mecanico;
import com.fiap.mecanica.gestao.core.gateway.MecanicoGateway;
import com.fiap.mecanica.gestao.infra.gateway.repository.MecanicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class MecanicoDatabaseGateway implements MecanicoGateway {

	private final MecanicoRepository mecanicoRepository;

	@Override
	public Optional<Mecanico> findById(Long id) {
		try {
			return mecanicoRepository.findById(id)
					.map(entity -> Mecanico.builder()
							.id(entity.getId())
							.nomeCompleto(new NomeCompleto(entity.getNome(), entity.getSobrenome()))
							.especialidade(entity.getEspecialidade())
							.build());
		} catch (Exception e) {
			log.error("Erro ao buscar mecanico por id: {}", id, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public Optional<Mecanico> findByUsuarioId(Long usuarioId) {
		try {
			return mecanicoRepository.findByUserId(usuarioId)
					.map(entity -> Mecanico.builder()
							.id(entity.getId())
							.nomeCompleto(new NomeCompleto(entity.getNome(), entity.getSobrenome()))
							.especialidade(entity.getEspecialidade())
							.build());
		} catch (Exception e) {
			log.error("Erro ao buscar mecanico por usuarioId: {}", usuarioId, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}
}
