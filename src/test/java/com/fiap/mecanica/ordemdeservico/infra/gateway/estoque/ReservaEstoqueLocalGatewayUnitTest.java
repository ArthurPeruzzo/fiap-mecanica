package com.fiap.mecanica.ordemdeservico.infra.gateway.estoque;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.domain.TipoItemReserva;
import com.fiap.mecanica.estoque.core.domain.UnidadeMedida;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.estoque.core.gateway.ReservaGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ReservaEstoqueLocalGatewayUnitTest {

    @Mock private PecaGateway pecaGateway;
    @Mock private InsumoGateway insumoGateway;
    @Mock private ReservaGateway reservaGateway;

    @InjectMocks
    private ReservaEstoqueLocalGateway gateway;

    @Test
    void reservarPeca_shouldReservarNoEstoque() {
        Mockito.when(pecaGateway.buscarPorId(5L)).thenReturn(Optional.of(Peca.builder().id(5L).nome("P").descricao("d").preco(BigDecimal.ONE).quantidadeEstoque(10).quantidadeReservada(0).build()));
        Mockito.when(reservaGateway.buscar(1L, TipoItemReserva.PECA, 5L)).thenReturn(Optional.empty());

        gateway.reservarPeca(1L, 5L, 2);

        Mockito.verify(pecaGateway).atualizar(Mockito.argThat(p -> p.getQuantidadeReservada() == 2));
    }

    @Test
    void reservarInsumo_shouldReservarNoEstoque() {
        Mockito.when(insumoGateway.buscarPorId(30L)).thenReturn(Optional.of(
                Insumo.builder().id(30L).nome("I").descricao("d").preco(BigDecimal.ONE).unidadeMedida(UnidadeMedida.LITRO).quantidadeEstoque(10).quantidadeReservada(0).build()));
        Mockito.when(reservaGateway.buscar(1L, TipoItemReserva.INSUMO, 30L)).thenReturn(Optional.empty());

        gateway.reservarInsumo(1L, 30L, 3);

        Mockito.verify(insumoGateway).atualizar(Mockito.argThat(i -> i.getQuantidadeReservada() == 3));
    }

    @Test
    void confirmarELiberar_shouldConsultarReservasAtivas() {
        Mockito.when(reservaGateway.listarAtivasPorOrdemDeServico(1L)).thenReturn(List.of());

        gateway.confirmarReservas(1L);
        gateway.liberarReservas(1L);

        Mockito.verify(reservaGateway, Mockito.times(2)).listarAtivasPorOrdemDeServico(1L);
    }
}
