package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.gestao.core.dto.ListarVeiculosDto;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import com.fiap.mecanica.shared.page.Pagina;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ListarVeiculosUseCaseUnitTest {

    @Mock
    private VeiculoGateway veiculoGateway;

    @Mock
    private ListarVeiculosOutputPort outputPort;

    private ListarVeiculosUseCase listarVeiculosUseCase;

    @BeforeEach
    void setUp() {
        listarVeiculosUseCase = new ListarVeiculosUseCase(veiculoGateway, outputPort);
    }

    @Test
    void shouldCallOutputPortWithPaginaFromGateway() {
        var veiculo = Veiculo.reconstituir(1L, 1L, "ABC1234", "Gol", 2020);
        var pagina = new Pagina<>(List.of(veiculo), 0, 10, 1L, 1);
        Mockito.when(veiculoGateway.listar(0, 10)).thenReturn(pagina);

        listarVeiculosUseCase.listar(new ListarVeiculosDto(0, 10));

        Mockito.verify(veiculoGateway).listar(0, 10);
        Mockito.verify(outputPort).apresentar(pagina);
    }

    @Test
    void shouldCallOutputPortWithEmptyPaginaWhenNoVeiculos() {
        var pagina = new Pagina<Veiculo>(List.of(), 0, 10, 0L, 0);
        Mockito.when(veiculoGateway.listar(0, 10)).thenReturn(pagina);

        listarVeiculosUseCase.listar(new ListarVeiculosDto(0, 10));

        Mockito.verify(outputPort).apresentar(pagina);
    }

    @Test
    void shouldPassPageAndSizeToGateway() {
        var pagina = new Pagina<Veiculo>(List.of(), 2, 5, 0L, 0);
        Mockito.when(veiculoGateway.listar(2, 5)).thenReturn(pagina);

        listarVeiculosUseCase.listar(new ListarVeiculosDto(2, 5));

        Mockito.verify(veiculoGateway).listar(2, 5);
    }
}
