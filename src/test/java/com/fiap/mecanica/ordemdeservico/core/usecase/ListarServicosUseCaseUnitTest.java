package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.dto.ListarServicosDto;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.servico.ListarServicosUseCase;
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
class ListarServicosUseCaseUnitTest {

    @InjectMocks
    private ListarServicosUseCase listarServicosUseCase;

    @Mock
    private ServicoGateway servicoGateway;

    @Test
    void shouldReturnPaginaFromGateway() {
        var servico = Servico.reconstituir(1L, "Troca de óleo", "Desc", new BigDecimal("150.00"));
        var pagina = new Pagina<>(List.of(servico), 0, 10, 1L, 1);
        Mockito.when(servicoGateway.listar(0, 10)).thenReturn(pagina);

        var resultado = listarServicosUseCase.listar(new ListarServicosDto(0, 10));

        assertEquals(1, resultado.content().size());
        assertEquals("Troca de óleo", resultado.content().getFirst().getNome());
        Mockito.verify(servicoGateway).listar(0, 10);
    }

    @Test
    void shouldReturnEmptyPaginaWhenNoServicos() {
        var pagina = new Pagina<Servico>(List.of(), 0, 10, 0L, 0);
        Mockito.when(servicoGateway.listar(0, 10)).thenReturn(pagina);

        var resultado = listarServicosUseCase.listar(new ListarServicosDto(0, 10));

        assertEquals(0, resultado.content().size());
    }
}
