package com.fiap.mecanica.gestao.infra.gateway.database.persona;

import com.fiap.mecanica.gestao.core.domain.persona.Atendente;
import com.fiap.mecanica.gestao.core.gateway.persona.AtendenteGateway;
import com.fiap.mecanica.gestao.infra.gateway.repository.persona.AtendenteRepository;
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
							.nome(entity.getNome())
							.sobrenome(entity.getSobrenome())
							.build());
		} catch (Exception e) {
			log.error("Erro ao buscar atendente por id: {}", id, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}
}
