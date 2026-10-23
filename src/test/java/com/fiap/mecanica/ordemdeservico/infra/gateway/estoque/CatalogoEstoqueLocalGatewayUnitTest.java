package com.fiap.mecanica.ordemdeservico.infra.gateway.estoque;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.domain.UnidadeMedida;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.ordemdeservico.core.dto.ItemCatalogo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CatalogoEstoqueLocalGatewayUnitTest {

    @Mock private PecaGateway pecaGateway;
    @Mock private InsumoGateway insumoGateway;

    @InjectMocks
    private CatalogoEstoqueLocalGateway gateway;

    private final Peca peca = Peca.builder().id(5L).nome("Filtro").descricao("d").preco(new BigDecimal("45.00")).quantidadeEstoque(10).build();
    private final Insumo insumo = Insumo.builder().id(30L).nome("Óleo").descricao("d").preco(new BigDecimal("35.00"))
            .unidadeMedida(UnidadeMedida.LITRO).quantidadeEstoque(10).build();

    @Test
    void buscarPeca_shouldRetornarApenasIdEPreco() {
        Mockito.when(pecaGateway.buscarPorId(5L)).thenReturn(Optional.of(peca));

        assertEquals(Optional.of(new ItemCatalogo(5L, new BigDecimal("45.00"))), gateway.buscarPeca(5L));
    }

    @Test
    void buscarPeca_shouldRetornarVazioWhenNaoExiste() {
        Mockito.when(pecaGateway.buscarPorId(9L)).thenReturn(Optional.empty());

        assertTrue(gateway.buscarPeca(9L).isEmpty());
    }

    @Test
    void buscarInsumo_shouldRetornarApenasIdEPreco() {
        Mockito.when(insumoGateway.buscarPorId(30L)).thenReturn(Optional.of(insumo));

        assertEquals(Optional.of(new ItemCatalogo(30L, new BigDecimal("35.00"))), gateway.buscarInsumo(30L));
    }

    @Test
    void listarPecasPorIds_shouldMapearParaItemCatalogo() {
        Mockito.when(pecaGateway.listarPorIds(List.of(5L))).thenReturn(List.of(peca));

        assertEquals(List.of(new ItemCatalogo(5L, new BigDecimal("45.00"))), gateway.listarPecasPorIds(List.of(5L)));
    }

    @Test
    void listarInsumosPorIds_shouldMapearParaItemCatalogo() {
        Mockito.when(insumoGateway.listarPorIds(List.of(30L))).thenReturn(List.of(insumo));

        assertEquals(List.of(new ItemCatalogo(30L, new BigDecimal("35.00"))), gateway.listarInsumosPorIds(List.of(30L)));
    }
}
