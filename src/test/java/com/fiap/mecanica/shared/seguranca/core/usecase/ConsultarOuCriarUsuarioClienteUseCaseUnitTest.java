package com.fiap.mecanica.shared.seguranca.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.shared.seguranca.core.domain.Role;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.core.domain.User;
import com.fiap.mecanica.shared.seguranca.core.domain.password.PasswordHash;
import com.fiap.mecanica.shared.seguranca.core.gateway.UserGateway;
import com.fiap.mecanica.shared.valueobjects.Cpf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ConsultarOuCriarUsuarioClienteUseCaseUnitTest {

    @Mock
    private ClienteGateway clienteGateway;

    @Mock
    private UserGateway userGateway;

    @Mock
    private ConsultarClienteOutputPort outputPort;

    private ConsultarOuCriarUsuarioClienteUseCase useCase;

    private static final String CPF = "52998224725";

    @BeforeEach
    void setUp() {
        useCase = new ConsultarOuCriarUsuarioClienteUseCase(clienteGateway, userGateway, outputPort);
    }

    private Cliente clientePadrao() {
        return Cliente.reconstituir(1L, "Pedro", null, CPF);
    }

    @Test
    void shouldThrowWhenClienteNotFound() {
        Mockito.when(clienteGateway.buscarPorCpf(CPF)).thenReturn(Optional.empty());

        Assertions.assertThrows(ClienteNaoEncontradoException.class, () -> useCase.consultar(CPF));

        Mockito.verifyNoInteractions(userGateway, outputPort);
    }

    @Test
    void shouldReuseExistingUserWithoutCreatingANewOne() {
        Mockito.when(clienteGateway.buscarPorCpf(CPF)).thenReturn(Optional.of(clientePadrao()));
        var userExistente = new User(10L, new Cpf(CPF), new PasswordHash("hash"),
                List.of(new Role(1L, RoleEnum.ROLE_CLIENTE)));
        Mockito.when(userGateway.findByCpf(CPF)).thenReturn(Optional.of(userExistente));

        useCase.consultar(CPF);

        Mockito.verify(userGateway, Mockito.never()).create(Mockito.any());
        Mockito.verify(outputPort).apresentar(10L);
    }

    @Test
    void shouldProvisionNewUserWithRoleClienteWhenNoneExists() {
        Mockito.when(clienteGateway.buscarPorCpf(CPF)).thenReturn(Optional.of(clientePadrao()));
        Mockito.when(userGateway.findByCpf(CPF)).thenReturn(Optional.empty());

        var userCriado = new User(20L, new Cpf(CPF), new PasswordHash("hash"),
                List.of(new Role(2L, RoleEnum.ROLE_CLIENTE)));
        Mockito.when(userGateway.create(Mockito.any(User.class))).thenReturn(userCriado);

        useCase.consultar(CPF);

        var captor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userGateway).create(captor.capture());
        var userProvisionado = captor.getValue();

        Assertions.assertEquals(CPF, userProvisionado.getCpf().getValor());
        Assertions.assertEquals(List.of(RoleEnum.ROLE_CLIENTE),
                userProvisionado.getRoles().stream().map(Role::getName).toList());
        Assertions.assertNotNull(userProvisionado.getPassword().getValue());

        Mockito.verify(outputPort).apresentar(20L);
    }
}
