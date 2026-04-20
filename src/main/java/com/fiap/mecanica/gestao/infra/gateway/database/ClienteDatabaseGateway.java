package com.fiap.mecanica.gestao.infra.gateway.database;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.gestao.infra.gateway.entity.ClienteEntity;
import com.fiap.mecanica.gestao.infra.gateway.repository.ClienteRepository;
import com.fiap.mecanica.shared.valueobjects.Cnpj;
import com.fiap.mecanica.shared.valueobjects.Cpf;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ClienteDatabaseGateway implements ClienteGateway {

	private final ClienteRepository clienteRepository;

	@Override
	public void criar(Cliente cliente) {
		var entity = ClienteEntity.builder()
				.nome(cliente.getNomeCompleto().nome())
				.sobrenome(cliente.getNomeCompleto().sobrenome())
				.cpf(cliente.getCpf().map(Cpf::getValor).orElse(null))
				.cnpj(cliente.getCnpj().map(Cnpj::getValor).orElse(null))
				.build();

		clienteRepository.save(entity);
	}
}
