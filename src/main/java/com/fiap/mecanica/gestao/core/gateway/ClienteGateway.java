package com.fiap.mecanica.gestao.core.gateway;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.shared.page.Pagina;

public interface ClienteGateway {
	void criar(Cliente cliente);
	boolean existePorCpf(String cpf);
	boolean existePorCnpj(String cnpj);
	Pagina<Cliente> listar(int page, int size);
}
