package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.gestao.core.dto.ListarVeiculosDto;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import com.fiap.mecanica.shared.page.Pagina;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ListarVeiculosUseCaseUnitTest {

    @InjectMocks
    private ListarVeiculosUseCase listarVeiculosUseCase;

    @Mock
    private VeiculoGateway veiculoGateway;

    @Test
    void shouldReturnPaginaFromGateway() {
        var veiculo = Veiculo.reconstituir(1L, 1L, "ABC1234", "Gol", 2020);
        var pagina = new Pagina<>(List.of(veiculo), 0, 10, 1L, 1);
        Mockito.when(veiculoGateway.listar(0, 10)).thenReturn(pagina);

        var resultado = listarVeiculosUseCase.listar(new ListarVeiculosDto(0, 10));

        assertEquals(1, resultado.content().size());
        assertEquals(1L, resultado.totalElements());
        Mockito.verify(veiculoGateway).listar(0, 10);
    }

    @Test
    void shouldReturnEmptyPaginaWhenNoVeiculos() {
        Mockito.when(veiculoGateway.listar(0, 10))
                .thenReturn(new Pagina<>(List.of(), 0, 10, 0L, 0));

        var resultado = listarVeiculosUseCase.listar(new ListarVeiculosDto(0, 10));

        assertTrue(resultado.content().isEmpty());
        assertEquals(0L, resultado.totalElements());
    }

    @Test
    void shouldPassPageAndSizeToGateway() {
        Mockito.when(veiculoGateway.listar(2, 5))
                .thenReturn(new Pagina<>(List.of(), 2, 5, 0L, 0));

        listarVeiculosUseCase.listar(new ListarVeiculosDto(2, 5));

        Mockito.verify(veiculoGateway).listar(2, 5);
    }
}
