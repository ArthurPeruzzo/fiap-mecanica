package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
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
class DeletarPecaUseCaseUnitTest {

    @InjectMocks
    private DeletarPecaUseCase deletarPecaUseCase;

    @Mock
    private PecaGateway pecaGateway;

    @Test
    void shouldCallDeleteWhenPecaExists() {
        var peca = Peca.reconstituir(1L, "Filtro", "Desc", new BigDecimal("20.00"), 5);
        Mockito.when(pecaGateway.buscarPorId(1L)).thenReturn(Optional.of(peca));

        deletarPecaUseCase.deletar(1L);

        Mockito.verify(pecaGateway).deletar(1L);
    }

    @Test
    void shouldThrowPecaNaoEncontradaExceptionWhenPecaDoesNotExist() {
        Mockito.when(pecaGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(PecaNaoEncontradaException.class, () -> deletarPecaUseCase.deletar(99L));

        Mockito.verify(pecaGateway, Mockito.never()).deletar(Mockito.anyLong());
    }
}
