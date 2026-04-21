package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.gestao.core.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DeletarVeiculoUseCaseUnitTest {

    @InjectMocks
    private DeletarVeiculoUseCase deletarVeiculoUseCase;

    @Mock
    private VeiculoGateway veiculoGateway;

    @Test
    void shouldDeletarVeiculoSuccessfully() {
        var veiculo = Veiculo.reconstituir(1L, 1L, "ABC1234", "Gol", 2020);
        Mockito.when(veiculoGateway.buscarPorId(1L)).thenReturn(Optional.of(veiculo));

        deletarVeiculoUseCase.deletar(1L);

        Mockito.verify(veiculoGateway).deletar(1L);
    }

    @Test
    void shouldThrowVeiculoNaoEncontradoExceptionWhenVeiculoNotFound() {
        Mockito.when(veiculoGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(VeiculoNaoEncontradoException.class,
                () -> deletarVeiculoUseCase.deletar(99L));

        Mockito.verify(veiculoGateway, Mockito.never()).deletar(Mockito.any());
    }
}
