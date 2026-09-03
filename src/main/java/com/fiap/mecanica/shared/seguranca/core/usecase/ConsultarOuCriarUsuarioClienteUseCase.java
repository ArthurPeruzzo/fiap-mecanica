package com.fiap.mecanica.shared.seguranca.core.usecase;

import com.fiap.mecanica.gestao.core.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.shared.seguranca.core.domain.Role;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.core.domain.User;
import com.fiap.mecanica.shared.seguranca.core.domain.password.Password;
import com.fiap.mecanica.shared.seguranca.core.gateway.UserGateway;
import com.fiap.mecanica.shared.valueobjects.Cpf;

import java.util.List;
import java.util.UUID;

public class ConsultarOuCriarUsuarioClienteUseCase {

    private final ClienteGateway clienteGateway;
    private final UserGateway userGateway;
    private final ConsultarClienteOutputPort outputPort;

    public ConsultarOuCriarUsuarioClienteUseCase(ClienteGateway clienteGateway,
                                                  UserGateway userGateway,
                                                  ConsultarClienteOutputPort outputPort) {
        this.clienteGateway = clienteGateway;
        this.userGateway = userGateway;
        this.outputPort = outputPort;
    }

    public void consultar(String cpf) {
        clienteGateway.buscarPorCpf(cpf).orElseThrow(ClienteNaoEncontradoException::new);
        User user = userGateway.findByCpf(cpf).orElseGet(() -> provisionar(cpf));
        outputPort.apresentar(user.getId());
    }

    private User provisionar(String cpf) {
        // Senha aleatória, deliberadamente não hasheada: nunca é usada para autenticação
        // real (o único caminho legítimo de emitir token para este User é a Function
        // Lambda, não POST /authenticate/login). Hashear exigiria injetar PasswordEncoder
        // (tipo do Spring Security) neste use case, violando a regra de core/ sem
        // dependência de Spring, sem nenhum ganho de segurança real.
        String senhaAleatoria = UUID.randomUUID().toString();
        return userGateway.create(new User(new Cpf(cpf), new Password(senhaAleatoria), List.of(new Role(RoleEnum.ROLE_CLIENTE))));
    }
}
