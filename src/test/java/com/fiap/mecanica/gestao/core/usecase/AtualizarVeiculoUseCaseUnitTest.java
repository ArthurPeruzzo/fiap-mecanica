package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.gestao.core.dto.AtualizarVeiculoDto;
import com.fiap.mecanica.gestao.core.exception.VeiculoJaExisteException;
import com.fiap.mecanica.gestao.core.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AtualizarVeiculoUseCaseUnitTest {

    @InjectMocks
    private AtualizarVeiculoUseCase atualizarVeiculoUseCase;

    @Mock
    private VeiculoGateway veiculoGateway;

    @Test
    void shouldUpdateVeiculoSuccessfully() {
        var veiculo = Veiculo.reconstituir(1L, 1L, "ABC1234", "Gol", 2020);
        var dto = new AtualizarVeiculoDto(1L, "ABC1D23", "Onix", 2023);

        Mockito.when(veiculoGateway.buscarPorId(1L)).thenReturn(Optional.of(veiculo));
        Mockito.when(veiculoGateway.existePorPlacaExcluindoId("ABC1D23", 1L)).thenReturn(false);

        atualizarVeiculoUseCase.atualizar(dto);

        Mockito.verify(veiculoGateway).atualizar(veiculo);
    }

    @Test
    void shouldUpdateVeiculoKeepingSamePlaca() {
        var veiculo = Veiculo.reconstituir(1L, 1L, "ABC1234", "Gol", 2020);
        var dto = new AtualizarVeiculoDto(1L, "ABC1234", "Gol Plus", 2021);

        Mockito.when(veiculoGateway.buscarPorId(1L)).thenReturn(Optional.of(veiculo));
        Mockito.when(veiculoGateway.existePorPlacaExcluindoId("ABC1234", 1L)).thenReturn(false);

        atualizarVeiculoUseCase.atualizar(dto);

        Mockito.verify(veiculoGateway).atualizar(veiculo);
    }

    @Test
    void shouldThrowVeiculoNaoEncontradoExceptionWhenVeiculoNotFound() {
        var dto = new AtualizarVeiculoDto(99L, "ABC1234", "Gol", 2020);

        Mockito.when(veiculoGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        Assertions.assertThrows(VeiculoNaoEncontradoException.class,
                () -> atualizarVeiculoUseCase.atualizar(dto));

        Mockito.verify(veiculoGateway, Mockito.never()).atualizar(Mockito.any());
    }

    @Test
    void shouldThrowVeiculoJaExisteExceptionWhenPlacaBelongsToAnotherVeiculo() {
        var veiculo = Veiculo.reconstituir(1L, 1L, "ABC1234", "Gol", 2020);
        var dto = new AtualizarVeiculoDto(1L, "XYZ9876", "Gol", 2020);

        Mockito.when(veiculoGateway.buscarPorId(1L)).thenReturn(Optional.of(veiculo));
        Mockito.when(veiculoGateway.existePorPlacaExcluindoId("XYZ9876", 1L)).thenReturn(true);

        Assertions.assertThrows(VeiculoJaExisteException.class,
                () -> atualizarVeiculoUseCase.atualizar(dto));

        Mockito.verify(veiculoGateway, Mockito.never()).atualizar(Mockito.any());
    }

    @Test
    void shouldUpdateDomainObjectFields() {
        var veiculo = Veiculo.reconstituir(1L, 1L, "ABC1234", "Gol", 2020);
        var dto = new AtualizarVeiculoDto(1L, "ABC1D23", "Onix", 2023);

        Mockito.when(veiculoGateway.buscarPorId(1L)).thenReturn(Optional.of(veiculo));
        Mockito.when(veiculoGateway.existePorPlacaExcluindoId("ABC1D23", 1L)).thenReturn(false);

        atualizarVeiculoUseCase.atualizar(dto);

        Assertions.assertEquals("ABC1D23", veiculo.getPlaca().getValor());
        Assertions.assertEquals("Onix", veiculo.getModelo());
        Assertions.assertEquals(2023, veiculo.getAno());
    }
}
