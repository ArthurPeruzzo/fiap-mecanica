package com.fiap.mecanica.estoque.core.domain;

import com.fiap.mecanica.estoque.core.exception.ReservaJaConfirmadaException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReservaUnitTest {

    @Test
    void novaReserva_shouldStartAtivaComQuantidadeZero() {
        var reserva = new Reserva(1L, TipoItemReserva.PECA, 5L);

        assertEquals(StatusReserva.ATIVA, reserva.getStatus());
        assertEquals(0, reserva.getQuantidade());
    }

    @Test
    void definirQuantidade_shouldLiberarWhenZero() {
        var reserva = new Reserva(1L, TipoItemReserva.PECA, 5L);
        reserva.definirQuantidade(3);

        reserva.definirQuantidade(0);

        assertEquals(StatusReserva.LIBERADA, reserva.getStatus());
        assertEquals(0, reserva.quantidadeAtiva());
    }

    @Test
    void definirQuantidade_shouldReativarReservaLiberada() {
        var reserva = Reserva.reconstituir(9L, 1L, TipoItemReserva.INSUMO, 5L, 2, StatusReserva.LIBERADA);

        reserva.definirQuantidade(4);

        assertEquals(StatusReserva.ATIVA, reserva.getStatus());
        assertEquals(4, reserva.quantidadeAtiva());
    }

    @Test
    void definirQuantidade_shouldThrowWhenConfirmada() {
        var reserva = Reserva.reconstituir(9L, 1L, TipoItemReserva.PECA, 5L, 2, StatusReserva.CONFIRMADA);

        assertThrows(ReservaJaConfirmadaException.class, () -> reserva.definirQuantidade(3));
    }

    @Test
    void quantidadeAtiva_shouldBeZeroWhenConfirmada() {
        var reserva = Reserva.reconstituir(9L, 1L, TipoItemReserva.PECA, 5L, 2, StatusReserva.ATIVA);

        reserva.confirmar();

        assertEquals(StatusReserva.CONFIRMADA, reserva.getStatus());
        assertEquals(0, reserva.quantidadeAtiva());
    }
}
