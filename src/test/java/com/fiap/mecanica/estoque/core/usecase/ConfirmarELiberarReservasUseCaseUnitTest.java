package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.domain.*;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.estoque.core.gateway.ReservaGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ConfirmarELiberarReservasUseCaseUnitTest {

    @Mock private PecaGateway pecaGateway;
    @Mock private InsumoGateway insumoGateway;
    @Mock private ReservaGateway reservaGateway;

    private static final Long ORDEM_ID = 1L;

    private Peca peca;
    private Insumo insumo;
    private Reserva reservaPeca;
    private Reserva reservaInsumo;

    private void stubReservasAtivas() {
        peca = Peca.reconstituir(5L, "Filtro", "desc", BigDecimal.TEN, 10, 3);
        insumo = Insumo.reconstituir(30L, "Óleo", "desc", BigDecimal.TEN, UnidadeMedida.LITRO, 20, 4);
        reservaPeca = Reserva.reconstituir(1L, ORDEM_ID, TipoItemReserva.PECA, 5L, 3, StatusReserva.ATIVA);
        reservaInsumo = Reserva.reconstituir(2L, ORDEM_ID, TipoItemReserva.INSUMO, 30L, 4, StatusReserva.ATIVA);
        Mockito.when(reservaGateway.listarAtivasPorOrdemDeServico(ORDEM_ID)).thenReturn(List.of(reservaPeca, reservaInsumo));
        Mockito.when(pecaGateway.buscarPorId(5L)).thenReturn(Optional.of(peca));
        Mockito.when(insumoGateway.buscarPorId(30L)).thenReturn(Optional.of(insumo));
    }

    @Test
    void confirmar_shouldBaixarEstoqueEConfirmarReservas() {
        stubReservasAtivas();

        new ConfirmarReservasOrdemDeServicoUseCase(pecaGateway, insumoGateway, reservaGateway).confirmar(ORDEM_ID);

        assertEquals(7, peca.getEstoqueTotal());
        assertEquals(0, peca.getQuantidadeReservada());
        assertEquals(16, insumo.getEstoqueTotal());
        assertEquals(0, insumo.getQuantidadeReservada());
        assertEquals(StatusReserva.CONFIRMADA, reservaPeca.getStatus());
        assertEquals(StatusReserva.CONFIRMADA, reservaInsumo.getStatus());
        Mockito.verify(pecaGateway).atualizar(peca);
        Mockito.verify(insumoGateway).atualizar(insumo);
        Mockito.verify(reservaGateway, Mockito.times(2)).salvar(Mockito.any());
    }

    @Test
    void liberar_shouldDevolverAoDisponivelSemAlterarTotal() {
        stubReservasAtivas();

        new LiberarReservasOrdemDeServicoUseCase(pecaGateway, insumoGateway, reservaGateway).liberar(ORDEM_ID);

        assertEquals(10, peca.getEstoqueTotal());
        assertEquals(0, peca.getQuantidadeReservada());
        assertEquals(20, insumo.getEstoqueTotal());
        assertEquals(0, insumo.getQuantidadeReservada());
        assertEquals(StatusReserva.LIBERADA, reservaPeca.getStatus());
        assertEquals(StatusReserva.LIBERADA, reservaInsumo.getStatus());
        Mockito.verify(reservaGateway, Mockito.times(2)).salvar(Mockito.any());
    }

    @Test
    void shouldNaoFazerNadaQuandoNaoHaReservasAtivas() {
        Mockito.when(reservaGateway.listarAtivasPorOrdemDeServico(ORDEM_ID)).thenReturn(List.of());

        new ConfirmarReservasOrdemDeServicoUseCase(pecaGateway, insumoGateway, reservaGateway).confirmar(ORDEM_ID);
        new LiberarReservasOrdemDeServicoUseCase(pecaGateway, insumoGateway, reservaGateway).liberar(ORDEM_ID);

        Mockito.verifyNoInteractions(pecaGateway, insumoGateway);
        Mockito.verify(reservaGateway, Mockito.never()).salvar(Mockito.any());
    }
}
