package com.fiap.mecanica.estoque.core.usecase;

import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.dto.ListarPecasDto;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.shared.page.Pagina;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ListarPecasUseCaseUnitTest {

    @InjectMocks
    private ListarPecasUseCase listarPecasUseCase;

    @Mock
    private PecaGateway pecaGateway;

    @Test
    void shouldDelegateToGatewayWithCorrectPageAndSize() {
        var pagina = new Pagina<Peca>(List.of(), 0, 10, 0L, 0);
        Mockito.when(pecaGateway.listar(0, 10)).thenReturn(pagina);

        var resultado = listarPecasUseCase.listar(new ListarPecasDto(0, 10));

        Mockito.verify(pecaGateway).listar(0, 10);
        assertEquals(0L, resultado.totalElements());
    }

    @Test
    void shouldReturnMappedPagina() {
        var peca = Peca.reconstituir(1L, "Filtro", "Desc", new BigDecimal("20.00"), 5);
        var pagina = new Pagina<>(List.of(peca), 0, 10, 1L, 1);
        Mockito.when(pecaGateway.listar(0, 10)).thenReturn(pagina);

        var resultado = listarPecasUseCase.listar(new ListarPecasDto(0, 10));

        assertEquals(1L, resultado.totalElements());
        assertEquals(1, resultado.content().size());
        assertEquals("Filtro", resultado.content().getFirst().getNome());
    }
}
