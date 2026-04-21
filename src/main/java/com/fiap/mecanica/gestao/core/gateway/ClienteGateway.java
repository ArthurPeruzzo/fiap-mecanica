package com.fiap.mecanica.gestao.core.gateway;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.shared.page.Pagina;

import java.util.Optional;

public interface ClienteGateway {
	void criar(Cliente cliente);
	void atualizar(Cliente cliente);
	Optional<Cliente> buscarPorId(Long id);
	boolean existePorCpf(String cpf);
	boolean existePorCnpj(String cnpj);
	boolean existePorCpfExcluindoId(String cpf, Long id);
	boolean existePorCnpjExcluindoId(String cnpj, Long id);
	Pagina<Cliente> listar(int page, int size);
	void deletar(Long id);
}
