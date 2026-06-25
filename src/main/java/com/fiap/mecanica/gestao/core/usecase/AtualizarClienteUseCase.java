package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.dto.AtualizarClienteDto;
import com.fiap.mecanica.gestao.core.exception.ClienteJaExisteException;
import com.fiap.mecanica.gestao.core.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;

public class AtualizarClienteUseCase {

    private final ClienteGateway clienteGateway;

    public AtualizarClienteUseCase(ClienteGateway clienteGateway) {
        this.clienteGateway = clienteGateway;
    }

    public void atualizar(AtualizarClienteDto dto) {
        var cliente = clienteGateway.buscarPorId(dto.id())
                .orElseThrow(ClienteNaoEncontradoException::new);

        cliente.atualizar(dto.nome(), dto.cnpj(), dto.cpf());

        cliente.getCpf().ifPresent(cpf -> {
            if (clienteGateway.existePorCpfExcluindoId(cpf.getValor(), dto.id())) {
                throw new ClienteJaExisteException();
            }
        });

        cliente.getCnpj().ifPresent(cnpj -> {
            if (clienteGateway.existePorCnpjExcluindoId(cnpj.getValor(), dto.id())) {
                throw new ClienteJaExisteException();
            }
        });

        clienteGateway.atualizar(cliente);
    }
}
