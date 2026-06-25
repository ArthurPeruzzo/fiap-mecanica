package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Atendente;
import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.domain.Mecanico;
import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.gestao.core.gateway.AtendenteGateway;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.gestao.core.gateway.MecanicoGateway;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.dto.OrdemDeServicoListagemDto;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.ListarOrdemDeServicoOutputPort;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.ListarOrdemDeServicoUseCase;
import com.fiap.mecanica.shared.page.Pagina;
import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ListarOrdemDeServicoUseCaseUnitTest {

    private ListarOrdemDeServicoUseCase listarOrdemDeServicoUseCase;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    @Mock
    private ClienteGateway clienteGateway;

    @Mock
    private VeiculoGateway veiculoGateway;

    @Mock
    private AtendenteGateway atendenteGateway;

    @Mock
    private MecanicoGateway mecanicoGateway;

    @Mock
    private ListarOrdemDeServicoOutputPort outputPort;

    @BeforeEach
    void setUp() {
        listarOrdemDeServicoUseCase = new ListarOrdemDeServicoUseCase(
                ordemDeServicoGateway, clienteGateway, veiculoGateway,
                atendenteGateway, mecanicoGateway, outputPort);
    }

    private static final Long ORDEM_ID = 1L;
    private static final Long CLIENTE_ID = 10L;
    private static final Long VEICULO_ID = 20L;
    private static final Long ATENDENTE_ID = 30L;
    private static final Long MECANICO_ID = 40L;

    private Cliente clientePadrao() {
        return Cliente.reconstituir(CLIENTE_ID,"Maria", null, "12345678909");
    }

    private Veiculo veiculoPadrao() {
        return Veiculo.reconstituir(VEICULO_ID, CLIENTE_ID, "ABC1234", "Civic", 2020);
    }

    private Atendente atendentePadrao() {
        return Atendente.builder().id(ATENDENTE_ID).nomeCompleto(new NomeCompleto("João", "Silva")).build();
    }

    private Mecanico mecanicoPadrao() {
        return Mecanico.builder().id(MECANICO_ID).nomeCompleto(new NomeCompleto("Carlos", "Lima")).build();
    }

    private OrdemDeServico ordemComMecanico() {
        return OrdemDeServico.builder()
                .id(ORDEM_ID)
                .clienteId(CLIENTE_ID)
                .veiculoId(VEICULO_ID)
                .atendenteId(ATENDENTE_ID)
                .mecanicoId(MECANICO_ID)
                .status(StatusOrdemDeServico.RECEBIDA)
                .descricao("Barulho ao frear")
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(null)
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of(new ServicoVinculado(1L, new BigDecimal("150.00"), StatusServico.NAO_INICIADO, null, null))))
                .pecasVinculadas(new ArrayList<>(List.of(new PecaVinculada(2L, 2, new BigDecimal("45.00")))))
                .insumosVinculados(new ArrayList<>(List.of(new InsumoVinculado(3L, 1, new BigDecimal("35.00")))))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.RECEBIDA))
                .build();
    }

    private OrdemDeServico ordemSemMecanico() {
        return OrdemDeServico.builder()
                .id(ORDEM_ID)
                .clienteId(CLIENTE_ID)
                .veiculoId(VEICULO_ID)
                .atendenteId(ATENDENTE_ID)
                .mecanicoId(null)
                .status(StatusOrdemDeServico.RECEBIDA)
                .descricao("Barulho ao frear")
                .dataCriacao(LocalDateTime.now())
                .dataInicioDiagnostico(null)
                .dataConclusaoDiagnostico(null)
                .servicosVinculados(new ArrayList<>(List.of()))
                .pecasVinculadas(new ArrayList<>(List.of()))
                .insumosVinculados(new ArrayList<>(List.of()))
                .orcamento(null)
                .dataEnvioOrcamento(null)
                .dataCancelamento(null)
                .dataAprovacao(null)
                .dataFinalizacao(null)
                .dataEntrega(null)
                .state(OrdemDeServicoStateFactory.from(StatusOrdemDeServico.RECEBIDA))
                .build();
    }

    private void stubGatewaysComMecanico() {
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(clientePadrao()));
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculoPadrao()));
        Mockito.when(atendenteGateway.findById(ATENDENTE_ID)).thenReturn(Optional.of(atendentePadrao()));
        Mockito.when(mecanicoGateway.findById(MECANICO_ID)).thenReturn(Optional.of(mecanicoPadrao()));
    }

    private void stubGatewaysSemMecanico() {
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(clientePadrao()));
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculoPadrao()));
        Mockito.when(atendenteGateway.findById(ATENDENTE_ID)).thenReturn(Optional.of(atendentePadrao()));
    }

    @SuppressWarnings("unchecked")
    private Pagina<OrdemDeServicoListagemDto> captureOutputPort() {
        var captor = ArgumentCaptor.forClass(Pagina.class);
        Mockito.verify(outputPort).apresentar(captor.capture());
        return (Pagina<OrdemDeServicoListagemDto>) captor.getValue();
    }

    @Test
    void shouldReturnEnrichedDtoWithAllFieldsWhenMecanicoIsPresent() {
        var pagina = new Pagina<>(List.of(ordemComMecanico()), 0, 10, 1L, 1);
        Mockito.when(ordemDeServicoGateway.listar(0, 10)).thenReturn(pagina);
        stubGatewaysComMecanico();

        listarOrdemDeServicoUseCase.listar(0, 10);

        var resultado = captureOutputPort();
        assertEquals(1, resultado.content().size());
        var dto = resultado.content().getFirst();
        assertEquals(ORDEM_ID, dto.getId());
        assertEquals("Maria", dto.getNomeCliente());
        assertEquals("123.456.789-09", dto.getDocumentoCliente());
        assertEquals("Civic 2020 ABC-1234", dto.getVeiculo());
        assertEquals("João Silva", dto.getNomeAtendente());
        assertEquals("Carlos Lima", dto.getNomeMecanico());
        assertEquals("RECEBIDA", dto.getStatus());
        assertEquals(1, dto.getServicos().size());
        assertEquals("NAO_INICIADO", dto.getServicos().getFirst().status());
        assertEquals(1, dto.getPecas().size());
        assertEquals(new BigDecimal("90.00"), dto.getPecas().getFirst().valorTotal());
        assertEquals(1, dto.getInsumos().size());
        assertEquals(new BigDecimal("35.00"), dto.getInsumos().getFirst().valorTotal());
    }

    @Test
    void shouldReturnNullNomeMecanicoWhenMecanicoIdIsNull() {
        var pagina = new Pagina<>(List.of(ordemSemMecanico()), 0, 10, 1L, 1);
        Mockito.when(ordemDeServicoGateway.listar(0, 10)).thenReturn(pagina);
        stubGatewaysSemMecanico();

        listarOrdemDeServicoUseCase.listar(0, 10);

        var resultado = captureOutputPort();
        assertNull(resultado.content().getFirst().getNomeMecanico());
        Mockito.verifyNoInteractions(mecanicoGateway);
    }

    @Test
    void shouldReturnEmptyPageWhenNoOrdensExist() {
        var pagina = new Pagina<OrdemDeServico>(List.of(), 0, 10, 0L, 0);
        Mockito.when(ordemDeServicoGateway.listar(0, 10)).thenReturn(pagina);

        listarOrdemDeServicoUseCase.listar(0, 10);

        var resultado = captureOutputPort();
        assertTrue(resultado.content().isEmpty());
        assertEquals(0L, resultado.totalElements());
        Mockito.verifyNoInteractions(clienteGateway, veiculoGateway, atendenteGateway, mecanicoGateway);
    }
}
