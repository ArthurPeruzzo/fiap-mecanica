package com.fiap.mecanica.gestao.core.gateway;

import com.fiap.mecanica.gestao.core.domain.Cliente;

public interface ClienteGateway {
	void criar(Cliente cliente);
	boolean existePorCpf(String cpf);
	boolean existePorCnpj(String cnpj);
}
