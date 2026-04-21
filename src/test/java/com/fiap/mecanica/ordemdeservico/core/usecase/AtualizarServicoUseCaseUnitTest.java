package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.dto.AtualizarServicoDto;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AtualizarServicoUseCaseUnitTest {

    @InjectMocks
    private AtualizarServicoUseCase atualizarServicoUseCase;

    @Mock
    private ServicoGateway servicoGateway;

    @Test
    void shouldUpdateServicoWithCorrectFields() {
        var existing = Servico.reconstituir(1L, "Troca de óleo", "Desc antiga", new BigDecimal("150.00"));
        Mockito.when(servicoGateway.buscarPorId(1L)).thenReturn(Optional.of(existing));
        var captor = ArgumentCaptor.forClass(Servico.class);

        atualizarServicoUseCase.atualizar(new AtualizarServicoDto(1L, "Alinhamento", "Desc nova", new BigDecimal("200.00")));

        Mockito.verify(servicoGateway).atualizar(captor.capture());
        var updated = captor.getValue();
        assertEquals("Alinhamento", updated.getNome());
        assertEquals("Desc nova", updated.getDescricao());
        assertEquals(new BigDecimal("200.00"), updated.getPreco());
    }

    @Test
    void shouldThrowWhenServicoNotFound() {
        Mockito.when(servicoGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(ServicoNaoEncontradoException.class,
                () -> atualizarServicoUseCase.atualizar(new AtualizarServicoDto(99L, "Nome", "Desc", new BigDecimal("10.00"))));

        Mockito.verify(servicoGateway, Mockito.never()).atualizar(Mockito.any());
    }
}
