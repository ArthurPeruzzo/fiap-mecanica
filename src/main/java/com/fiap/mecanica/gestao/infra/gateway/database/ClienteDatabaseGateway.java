package com.fiap.mecanica.gestao.infra.gateway.database;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.gestao.infra.gateway.entity.ClienteEntity;
import com.fiap.mecanica.gestao.infra.gateway.repository.ClienteRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import com.fiap.mecanica.shared.page.Pagina;
import com.fiap.mecanica.shared.valueobjects.Cnpj;
import com.fiap.mecanica.shared.valueobjects.Cpf;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ClienteDatabaseGateway implements ClienteGateway {

	private final ClienteRepository clienteRepository;

	@Override
	public void criar(Cliente cliente) {
		try {
			var entity = ClienteEntity.builder()
					.nome(cliente.getNome())
					.cpf(cliente.getCpf().map(Cpf::getValor).orElse(null))
					.cnpj(cliente.getCnpj().map(Cnpj::getValor).orElse(null))
					.build();
			clienteRepository.save(entity);
		} catch (Exception e) {
			log.error("Erro ao criar cliente", e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public Optional<Cliente> buscarPorId(Long id) {
		try {
			return clienteRepository.findById(id)
					.map(e -> Cliente.reconstituir(
							e.getId(),
							e.getNome(),
							e.getCnpj(),
							e.getCpf()));
		} catch (Exception e) {
			log.error("Erro ao buscar cliente por id: {}", id, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public Optional<Cliente> buscarPorCpf(String cpf) {
		try {
			return clienteRepository.findByCpf(cpf)
					.map(e -> Cliente.reconstituir(
							e.getId(),
							e.getNome(),
							e.getCnpj(),
							e.getCpf()));
		} catch (Exception e) {
			log.error("Erro ao buscar cliente por cpf: {}", cpf, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public void atualizar(Cliente cliente) {
		try {
			var entity = ClienteEntity.builder()
					.id(cliente.getId())
					.nome(cliente.getNome())
					.cpf(cliente.getCpf().map(Cpf::getValor).orElse(null))
					.cnpj(cliente.getCnpj().map(Cnpj::getValor).orElse(null))
					.build();
			clienteRepository.save(entity);
		} catch (Exception e) {
			log.error("Erro ao atualizar cliente", e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public boolean existePorCpfExcluindoId(String cpf, Long id) {
		try {
			return clienteRepository.existsByCpfAndIdNot(cpf, id);
		} catch (Exception e) {
			log.error("Erro ao verificar existência de cliente por cpf excluindo id", e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public boolean existePorCnpjExcluindoId(String cnpj, Long id) {
		try {
			return clienteRepository.existsByCnpjAndIdNot(cnpj, id);
		} catch (Exception e) {
			log.error("Erro ao verificar existência de cliente por cnpj excluindo id", e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}

	@Override
	public Pagina<Cliente> listar(int page, int size) {
		try {
			var resultado = clienteRepository.findAll(PageRequest.of(page, size));
			var clientes = resultado.getContent().stream()
					.map(e -> Cliente.reconstituir(
							e.getId(),
							e.getNome(),
							e.getCnpj(),
							e.getCpf()))
					.toList();
			return new Pagina<>(clientes, resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(), resultado.getTotalPages());
		} catch (Exception e) {
			log.error("Erro ao listar clientes", e);
			throw new ErroAcessoBaseDeDadosException();
		}
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

	@Override
	public void deletar(Long id) {
		try {
			clienteRepository.deleteById(id);
		} catch (Exception e) {
			log.error("Erro ao deletar cliente por id: {}", id, e);
			throw new ErroAcessoBaseDeDadosException();
		}
	}
}
