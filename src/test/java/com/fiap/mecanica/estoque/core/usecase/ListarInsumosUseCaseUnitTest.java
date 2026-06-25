package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.domain.UnidadeMedida;
import com.fiap.mecanica.estoque.core.dto.ListarInsumosDto;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.shared.page.Pagina;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ListarInsumosUseCaseUnitTest {

    @Mock
    private InsumoGateway insumoGateway;

    @Mock
    private ListarInsumosOutputPort outputPort;

    private ListarInsumosUseCase listarInsumosUseCase;

    @BeforeEach
    void setUp() {
        listarInsumosUseCase = new ListarInsumosUseCase(insumoGateway, outputPort);
    }

    @Test
    void shouldDelegateToGatewayWithCorrectPageAndSize() {
        var pagina = new Pagina<Insumo>(List.of(), 0, 10, 0L, 0);
        Mockito.when(insumoGateway.listar(0, 10)).thenReturn(pagina);

        listarInsumosUseCase.listar(new ListarInsumosDto(0, 10));

        Mockito.verify(insumoGateway).listar(0, 10);
        Mockito.verify(outputPort).apresentar(pagina);
    }

    @Test
    void shouldCallOutputPortWithPaginaFromGateway() {
        var insumo = Insumo.reconstituir(1L, "Óleo", "Desc", new BigDecimal("30.00"), UnidadeMedida.LITRO, 5);
        var pagina = new Pagina<>(List.of(insumo), 0, 10, 1L, 1);
        Mockito.when(insumoGateway.listar(0, 10)).thenReturn(pagina);

        listarInsumosUseCase.listar(new ListarInsumosDto(0, 10));

        Mockito.verify(outputPort).apresentar(pagina);
    }
}
