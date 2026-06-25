package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.dto.ListarServicosDto;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.servico.ListarServicosOutputPort;
import com.fiap.mecanica.ordemdeservico.core.usecase.servico.ListarServicosUseCase;
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
class ListarServicosUseCaseUnitTest {

    @Mock
    private ServicoGateway servicoGateway;

    @Mock
    private ListarServicosOutputPort outputPort;

    private ListarServicosUseCase listarServicosUseCase;

    @BeforeEach
    void setUp() {
        listarServicosUseCase = new ListarServicosUseCase(servicoGateway, outputPort);
    }

    @Test
    void shouldCallOutputPortWithPaginaFromGateway() {
        var servico = Servico.reconstituir(1L, "Troca de óleo", "Desc", new BigDecimal("150.00"));
        var pagina = new Pagina<>(List.of(servico), 0, 10, 1L, 1);
        Mockito.when(servicoGateway.listar(0, 10)).thenReturn(pagina);

        listarServicosUseCase.listar(new ListarServicosDto(0, 10));

        Mockito.verify(servicoGateway).listar(0, 10);
        Mockito.verify(outputPort).apresentar(pagina);
    }

    @Test
    void shouldCallOutputPortWithEmptyPaginaWhenNoServicos() {
        var pagina = new Pagina<Servico>(List.of(), 0, 10, 0L, 0);
        Mockito.when(servicoGateway.listar(0, 10)).thenReturn(pagina);

        listarServicosUseCase.listar(new ListarServicosDto(0, 10));

        Mockito.verify(outputPort).apresentar(pagina);
    }
}
