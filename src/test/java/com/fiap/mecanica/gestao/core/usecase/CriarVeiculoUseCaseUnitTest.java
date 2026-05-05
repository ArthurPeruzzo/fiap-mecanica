package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.dto.CriarVeiculoDto;
import com.fiap.mecanica.gestao.core.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.exception.VeiculoJaExisteException;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
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
class CriarVeiculoUseCaseUnitTest {

    @InjectMocks
    private CriarVeiculoUseCase criarVeiculoUseCase;

    @Mock
    private VeiculoGateway veiculoGateway;

    @Mock
    private ClienteGateway clienteGateway;

    @Test
    void shouldCreateVeiculoSuccessfully() {
        var cliente = Cliente.reconstituir(1L, "Pedro", null, "12345678909");
        var dto = new CriarVeiculoDto(1L, "ABC1234", "Gol", 2020);

        Mockito.when(clienteGateway.buscarPorId(1L)).thenReturn(Optional.of(cliente));
        Mockito.when(veiculoGateway.existePorPlaca("ABC1234")).thenReturn(false);

        criarVeiculoUseCase.criar(dto);

        Mockito.verify(veiculoGateway).criar(Mockito.any());
    }

    @Test
    void shouldThrowClienteNaoEncontradoExceptionWhenClienteNotFound() {
        var dto = new CriarVeiculoDto(99L, "ABC1234", "Gol", 2020);

        Mockito.when(clienteGateway.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class, () -> criarVeiculoUseCase.criar(dto));

        Mockito.verify(veiculoGateway, Mockito.never()).criar(Mockito.any());
    }

    @Test
    void shouldThrowVeiculoJaExisteExceptionWhenPlacaAlreadyExists() {
        var cliente = Cliente.reconstituir(1L, "Pedro", null, "12345678909");
        var dto = new CriarVeiculoDto(1L, "ABC1234", "Gol", 2020);

        Mockito.when(clienteGateway.buscarPorId(1L)).thenReturn(Optional.of(cliente));
        Mockito.when(veiculoGateway.existePorPlaca("ABC1234")).thenReturn(true);

        assertThrows(VeiculoJaExisteException.class, () -> criarVeiculoUseCase.criar(dto));

        Mockito.verify(veiculoGateway, Mockito.never()).criar(Mockito.any());
    }

    @Test
    void shouldStripHyphenFromPlacaBeforeCheckingDuplicate() {
        var cliente = Cliente.reconstituir(1L,"Pedro", null, "12345678909");
        var dto = new CriarVeiculoDto(1L, "ABC-1234", "Gol", 2020);

        Mockito.when(clienteGateway.buscarPorId(1L)).thenReturn(Optional.of(cliente));
        Mockito.when(veiculoGateway.existePorPlaca("ABC1234")).thenReturn(false);

        criarVeiculoUseCase.criar(dto);

        Mockito.verify(veiculoGateway).existePorPlaca("ABC1234");
    }
}
