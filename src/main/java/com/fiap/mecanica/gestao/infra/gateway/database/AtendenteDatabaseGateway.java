package com.fiap.mecanica.gestao.infra.gateway.database;

import com.fiap.mecanica.gestao.core.domain.Atendente;
import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
import com.fiap.mecanica.gestao.core.gateway.AtendenteGateway;
import com.fiap.mecanica.gestao.infra.gateway.repository.AtendenteRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class AtendenteDatabaseGateway implements AtendenteGateway {

	private final AtendenteRepository atendenteRepository;

	@Override
	public Optional<Atendente> findById(Long id) {
		try {
			return atendenteRepository.findById(id)
					.map(entity -> Atendente.builder()
							.id(entity.getId())
							.nomeCompleto(new NomeCompleto(entity.getNome(), entity.getSobrenome()))
							.build());
		} catch (Exception e) {
			log.error("Erro ao buscar atendente por id: {}", id, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public Optional<Atendente> findByUsuarioId(Long usuarioId) {
		try {
			return atendenteRepository.findByUserId(usuarioId)
					.map(entity -> Atendente.builder()
							.id(entity.getId())
							.nomeCompleto(new NomeCompleto(entity.getNome(), entity.getSobrenome()))
							.build());
		} catch (Exception e) {
			log.error("Erro ao buscar atendente por usuarioId: {}", usuarioId, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}
}
