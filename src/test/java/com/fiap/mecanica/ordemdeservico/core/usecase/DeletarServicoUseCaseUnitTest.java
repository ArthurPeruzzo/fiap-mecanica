package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
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
class DeletarServicoUseCaseUnitTest {

    @InjectMocks
    private DeletarServicoUseCase deletarServicoUseCase;

    @Mock
    private ServicoGateway servicoGateway;

    @Test
    void shouldDelegateDeleteToGateway() {
        var servico = Servico.reconstituir(1L, "Troca de óleo", "Desc", new BigDecimal("150.00"));
        Mockito.when(servicoGateway.buscarPorId(1L)).thenReturn(Optional.of(servico));

        deletarServicoUseCase.deletar(1L);

        Mockito.verify(servicoGateway).deletar(1L);
    }

    @Test
    void shouldThrowWhenServicoNotFound() {
        Mockito.when(servicoGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(ServicoNaoEncontradoException.class, () -> deletarServicoUseCase.deletar(99L));

        Mockito.verify(servicoGateway, Mockito.never()).deletar(Mockito.anyLong());
    }
}
