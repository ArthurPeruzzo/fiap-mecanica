package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.domain.*;
import com.fiap.mecanica.estoque.core.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.mecanica.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.estoque.core.gateway.ReservaGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReservarItemEstoqueUseCaseUnitTest {

    @Mock private PecaGateway pecaGateway;
    @Mock private InsumoGateway insumoGateway;
    @Mock private ReservaGateway reservaGateway;

    private ReservarItemEstoqueUseCase useCase;

    private static final Long ORDEM_ID = 1L;
    private static final Long PECA_ID = 5L;
    private static final Long INSUMO_ID = 30L;

    @BeforeEach
    void setUp() {
        useCase = new ReservarItemEstoqueUseCase(pecaGateway, insumoGateway, reservaGateway);
    }

    private Peca peca(int estoque, int reservada) {
        return Peca.reconstituir(PECA_ID, "Filtro", "desc", BigDecimal.TEN, estoque, reservada);
    }

    @Test
    void shouldCriarReservaENaoAlterarEstoqueTotal() {
        var peca = peca(10, 0);
        Mockito.when(pecaGateway.buscarPorId(PECA_ID)).thenReturn(Optional.of(peca));
        Mockito.when(reservaGateway.buscar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID)).thenReturn(Optional.empty());

        useCase.reservar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID, 3);

        assertEquals(10, peca.getEstoqueTotal());
        assertEquals(3, peca.getQuantidadeReservada());
        Mockito.verify(pecaGateway).atualizar(peca);
        var captor = ArgumentCaptor.forClass(Reserva.class);
        Mockito.verify(reservaGateway).salvar(captor.capture());
        assertEquals(3, captor.getValue().getQuantidade());
        assertEquals(StatusReserva.ATIVA, captor.getValue().getStatus());
    }

    @Test
    void shouldReservarSomenteADiferencaQuandoTotalAumenta() {
        var peca = peca(10, 2);
        Mockito.when(pecaGateway.buscarPorId(PECA_ID)).thenReturn(Optional.of(peca));
        Mockito.when(reservaGateway.buscar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID)).thenReturn(Optional.of(
                Reserva.reconstituir(7L, ORDEM_ID, TipoItemReserva.PECA, PECA_ID, 2, StatusReserva.ATIVA)));

        useCase.reservar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID, 5);

        assertEquals(5, peca.getQuantidadeReservada());
    }

    @Test
    void shouldLiberarADiferencaQuandoTotalDiminui() {
        var peca = peca(10, 5);
        Mockito.when(pecaGateway.buscarPorId(PECA_ID)).thenReturn(Optional.of(peca));
        Mockito.when(reservaGateway.buscar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID)).thenReturn(Optional.of(
                Reserva.reconstituir(7L, ORDEM_ID, TipoItemReserva.PECA, PECA_ID, 5, StatusReserva.ATIVA)));

        useCase.reservar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID, 0);

        assertEquals(0, peca.getQuantidadeReservada());
        var captor = ArgumentCaptor.forClass(Reserva.class);
        Mockito.verify(reservaGateway).salvar(captor.capture());
        assertEquals(StatusReserva.LIBERADA, captor.getValue().getStatus());
    }

    @Test
    void shouldSerIdempotenteQuandoTotalNaoMuda() {
        Mockito.when(pecaGateway.buscarPorId(PECA_ID)).thenReturn(Optional.of(peca(10, 3)));
        Mockito.when(reservaGateway.buscar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID)).thenReturn(Optional.of(
                Reserva.reconstituir(7L, ORDEM_ID, TipoItemReserva.PECA, PECA_ID, 3, StatusReserva.ATIVA)));

        useCase.reservar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID, 3);

        Mockito.verify(pecaGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verify(reservaGateway, Mockito.never()).salvar(Mockito.any());
    }

    @Test
    void shouldThrowEstoqueInsuficienteSemPersistir() {
        Mockito.when(pecaGateway.buscarPorId(PECA_ID)).thenReturn(Optional.of(peca(10, 9)));
        Mockito.when(reservaGateway.buscar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID)).thenReturn(Optional.empty());

        assertThrows(EstoqueInsuficienteException.class,
                () -> useCase.reservar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID, 2));

        Mockito.verify(pecaGateway, Mockito.never()).atualizar(Mockito.any());
        Mockito.verify(reservaGateway, Mockito.never()).salvar(Mockito.any());
    }

    @Test
    void shouldReservarInsumo() {
        var insumo = Insumo.reconstituir(INSUMO_ID, "Óleo", "desc", BigDecimal.TEN, UnidadeMedida.LITRO, 20, 0);
        Mockito.when(insumoGateway.buscarPorId(INSUMO_ID)).thenReturn(Optional.of(insumo));
        Mockito.when(reservaGateway.buscar(ORDEM_ID, TipoItemReserva.INSUMO, INSUMO_ID)).thenReturn(Optional.empty());

        useCase.reservar(ORDEM_ID, TipoItemReserva.INSUMO, INSUMO_ID, 4);

        assertEquals(4, insumo.getQuantidadeReservada());
        Mockito.verify(insumoGateway).atualizar(insumo);
        Mockito.verifyNoInteractions(pecaGateway);
    }

    @Test
    void shouldThrowWhenPecaNotFound() {
        Mockito.when(pecaGateway.buscarPorId(PECA_ID)).thenReturn(Optional.empty());

        assertThrows(PecaNaoEncontradaException.class,
                () -> useCase.reservar(ORDEM_ID, TipoItemReserva.PECA, PECA_ID, 1));
        Mockito.verifyNoInteractions(reservaGateway);
    }

    @Test
    void shouldThrowWhenInsumoNotFound() {
        Mockito.when(insumoGateway.buscarPorId(INSUMO_ID)).thenReturn(Optional.empty());

        assertThrows(InsumoNaoEncontradoException.class,
                () -> useCase.reservar(ORDEM_ID, TipoItemReserva.INSUMO, INSUMO_ID, 1));
        Mockito.verifyNoInteractions(reservaGateway);
    }
}
