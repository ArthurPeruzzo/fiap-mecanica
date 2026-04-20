package com.fiap.mecanica.gestao.infra.gateway.database;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.gestao.infra.gateway.entity.ClienteEntity;
import com.fiap.mecanica.gestao.infra.gateway.repository.ClienteRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
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

	@Override
	public boolean existePorCpf(String cpf) {
		try {
			return clienteRepository.existsByCpf(cpf);
		} catch (Exception e) {
			log.error("Erro ao verificar existência de cliente por cpf", e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public boolean existePorCnpj(String cnpj) {
		try {
			return clienteRepository.existsByCnpj(cnpj);
		} catch (Exception e) {
			log.error("Erro ao verificar existência de cliente por cnpj", e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}
}
