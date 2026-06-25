package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.dto.ListarClientesDto;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.shared.page.Pagina;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ListarClientesUseCaseUnitTest {

    @Mock
    private ClienteGateway clienteGateway;

    @Mock
    private ListarClientesOutputPort outputPort;

    private ListarClientesUseCase listarClientesUseCase;

    @BeforeEach
    void setUp() {
        listarClientesUseCase = new ListarClientesUseCase(clienteGateway, outputPort);
    }

    @Test
    void shouldDelegateToGatewayWithCorrectPageAndSize() {
        var pagina = new Pagina<Cliente>(List.of(), 0, 10, 0L, 0);
        Mockito.when(clienteGateway.listar(0, 10)).thenReturn(pagina);

        listarClientesUseCase.listar(new ListarClientesDto(0, 10));

        Mockito.verify(clienteGateway).listar(0, 10);
        Mockito.verifyNoMoreInteractions(clienteGateway);
    }

    @Test
    void shouldCallOutputPortWithPaginaFromGateway() {
        var cliente = Cliente.reconstituir(1L, "Pedro", null, "12345678909");
        var pagina = new Pagina<>(List.of(cliente), 0, 10, 1L, 1);
        Mockito.when(clienteGateway.listar(0, 10)).thenReturn(pagina);

        listarClientesUseCase.listar(new ListarClientesDto(0, 10));

        Mockito.verify(outputPort).apresentar(pagina);
    }

    @Test
    void shouldCallOutputPortWithEmptyPaginaWhenNoClientes() {
        var pagina = new Pagina<Cliente>(List.of(), 0, 10, 0L, 0);
        Mockito.when(clienteGateway.listar(0, 10)).thenReturn(pagina);

        listarClientesUseCase.listar(new ListarClientesDto(0, 10));

        Mockito.verify(outputPort).apresentar(pagina);
    }
}
