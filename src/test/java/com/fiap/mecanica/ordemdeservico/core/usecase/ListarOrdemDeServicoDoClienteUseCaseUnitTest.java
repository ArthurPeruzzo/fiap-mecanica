package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Atendente;
import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.gestao.core.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.AtendenteGateway;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.gestao.core.gateway.MecanicoGateway;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServicoStateFactory;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.dto.OrdemDeServicoListagemDto;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.ListarOrdemDeServicoDoClienteUseCase;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.ListarOrdemDeServicoOutputPort;
import com.fiap.mecanica.shared.page.Pagina;
import com.fiap.mecanica.shared.seguranca.core.domain.Role;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.core.domain.User;
import com.fiap.mecanica.shared.seguranca.core.domain.password.PasswordHash;
import com.fiap.mecanica.shared.seguranca.core.exception.UserNotFoundException;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.UserGateway;
import com.fiap.mecanica.shared.valueobjects.Cpf;
import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ListarOrdemDeServicoDoClienteUseCaseUnitTest {

    private ListarOrdemDeServicoDoClienteUseCase useCase;

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
    private TokenGateway tokenGateway;

    @Mock
    private UserGateway userGateway;

    @Mock
    private ListarOrdemDeServicoOutputPort outputPort;

    private static final Long USER_ID = 99L;
    private static final Long CLIENTE_ID = 10L;
    private static final Long VEICULO_ID = 20L;
    private static final Long ATENDENTE_ID = 30L;
    private static final Long ORDEM_ID = 1L;
    private static final String CPF = "52998224725";

    @BeforeEach
    void setUp() {
        useCase = new ListarOrdemDeServicoDoClienteUseCase(ordemDeServicoGateway, clienteGateway, veiculoGateway,
                atendenteGateway, mecanicoGateway, tokenGateway, userGateway, outputPort);
    }

    private User userPadrao() {
        return new User(USER_ID, new Cpf(CPF), new PasswordHash("hash"), List.of(new Role(1L, RoleEnum.ROLE_CLIENTE)));
    }

    private Cliente clientePadrao() {
        return Cliente.reconstituir(CLIENTE_ID, "Maria", null, CPF);
    }

    private Veiculo veiculoPadrao() {
        return Veiculo.reconstituir(VEICULO_ID, CLIENTE_ID, "ABC1234", "Civic", 2020);
    }

    private Atendente atendentePadrao() {
        return Atendente.builder().id(ATENDENTE_ID).nomeCompleto(new NomeCompleto("João", "Silva")).build();
    }

    private OrdemDeServico ordemDoCliente() {
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

    @SuppressWarnings("unchecked")
    private Pagina<OrdemDeServicoListagemDto> captureOutputPort() {
        var captor = ArgumentCaptor.forClass(Pagina.class);
        Mockito.verify(outputPort).apresentar(captor.capture());
        return (Pagina<OrdemDeServicoListagemDto>) captor.getValue();
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(userGateway.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> useCase.listar(0, 10));

        Mockito.verifyNoInteractions(clienteGateway, ordemDeServicoGateway, outputPort);
    }

    @Test
    void shouldThrowWhenClienteNotFoundByUserCpf() {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(userGateway.findById(USER_ID)).thenReturn(Optional.of(userPadrao()));
        Mockito.when(clienteGateway.buscarPorCpf(CPF)).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class, () -> useCase.listar(0, 10));

        Mockito.verifyNoInteractions(ordemDeServicoGateway, outputPort);
    }

    @Test
    void shouldListOnlyOrdersFilteredByResolvedClienteId() {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(userGateway.findById(USER_ID)).thenReturn(Optional.of(userPadrao()));
        Mockito.when(clienteGateway.buscarPorCpf(CPF)).thenReturn(Optional.of(clientePadrao()));

        var pagina = new Pagina<>(List.of(ordemDoCliente()), 0, 10, 1L, 1);
        Mockito.when(ordemDeServicoGateway.listarPorClienteId(CLIENTE_ID, 0, 10)).thenReturn(pagina);
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(clientePadrao()));
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculoPadrao()));
        Mockito.when(atendenteGateway.findById(ATENDENTE_ID)).thenReturn(Optional.of(atendentePadrao()));

        useCase.listar(0, 10);

        Mockito.verify(ordemDeServicoGateway).listarPorClienteId(CLIENTE_ID, 0, 10);
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).listar(Mockito.anyInt(), Mockito.anyInt());

        var resultado = captureOutputPort();
        assertEquals(1, resultado.content().size());
        var dto = resultado.content().getFirst();
        assertEquals(ORDEM_ID, dto.getId());
        assertEquals("Maria", dto.getNomeCliente());
        assertNull(dto.getNomeMecanico());
    }
}
