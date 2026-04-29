package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.domain.UnidadeMedida;
import com.fiap.mecanica.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DeletarInsumoUseCaseUnitTest {

    @InjectMocks
    private DeletarInsumoUseCase deletarInsumoUseCase;

    @Mock
    private InsumoGateway insumoGateway;

    @Test
    void shouldCallDeleteWhenInsumoExists() {
        var insumo = Insumo.reconstituir(1L, "Óleo", "Desc", new BigDecimal("30.00"), UnidadeMedida.LITRO, 5);
        Mockito.when(insumoGateway.buscarPorId(1L)).thenReturn(Optional.of(insumo));

        deletarInsumoUseCase.deletar(1L);

        Mockito.verify(insumoGateway).deletar(1L);
    }

    @Test
    void shouldThrowInsumoNaoEncontradoExceptionWhenInsumoDoesNotExist() {
        Mockito.when(insumoGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(InsumoNaoEncontradoException.class, () -> deletarInsumoUseCase.deletar(99L));

        Mockito.verify(insumoGateway, Mockito.never()).deletar(Mockito.anyLong());
    }
}
