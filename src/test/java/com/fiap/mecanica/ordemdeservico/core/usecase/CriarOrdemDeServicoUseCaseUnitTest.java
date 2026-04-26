package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Atendente;
import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.gestao.core.exception.AtendenteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.AtendenteGateway;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.dto.CriarOrdemDeServicoDto;
import com.fiap.mecanica.ordemdeservico.core.exception.VeiculoNaoPertenceAoClienteException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CriarOrdemDeServicoUseCaseUnitTest {

    @InjectMocks
    private CriarOrdemDeServicoUseCase criarOrdemDeServicoUseCase;

    @Mock
    private AtendenteGateway atendenteGateway;

    @Mock
    private TokenGateway tokenGateway;

    @Mock
    private OrdemDeServicoGateway ordemDeServicoGateway;

    @Mock
    private VeiculoGateway veiculoGateway;

    @Mock
    private ClienteGateway clienteGateway;

    private static final Long USER_ID = 10L;
    private static final Long ATENDENTE_ID = 3L;
    private static final Long CLIENTE_ID = 1L;
    private static final Long VEICULO_ID = 2L;

    private Atendente atendentePadrao() {
        return Atendente.builder().id(ATENDENTE_ID).build();
    }

    private Cliente clientePadrao() {
        return Cliente.reconstituir(CLIENTE_ID, new NomeCompleto("Pedro", "Silva"), null, "12345678909");
    }

    private Veiculo veiculoPadrao() {
        return Veiculo.reconstituir(VEICULO_ID, CLIENTE_ID, "ABC1234", "Gol", 2020);
    }

    private void stubAtendente() {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(atendenteGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.of(atendentePadrao()));
    }

    private void stubClienteEVeiculo() {
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculoPadrao()));
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(clientePadrao()));
    }

    @Test
    void shouldCreateOrdemDeServicoWithCorrectFields() {
        stubAtendente();
        stubClienteEVeiculo();

        var dto = new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID);
        var captor = ArgumentCaptor.forClass(OrdemDeServico.class);

        criarOrdemDeServicoUseCase.criar(dto);

        Mockito.verify(ordemDeServicoGateway).criar(captor.capture());
        var os = captor.getValue();
        assertEquals(CLIENTE_ID, os.getClienteId());
        assertEquals(VEICULO_ID, os.getVeiculoId());
        assertEquals(ATENDENTE_ID, os.getAtendenteId());
        assertEquals(StatusOrdemDeServico.RECEBIDA, os.getStatus());
        assertNotNull(os.getDataCriacao());
    }

    @Test
    void shouldThrowWhenAtendenteNotFound() {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(atendenteGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.empty());
        CriarOrdemDeServicoDto criarOrdemDeServicoDto = new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID);

        assertThrows(AtendenteNaoEncontradoException.class,
                () -> criarOrdemDeServicoUseCase.criar(criarOrdemDeServicoDto));

        Mockito.verifyNoInteractions(veiculoGateway, clienteGateway, ordemDeServicoGateway);
    }

    @Test
    void shouldThrowWhenVeiculoNotFound() {
        stubAtendente();
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.empty());
        CriarOrdemDeServicoDto criarOrdemDeServicoDto = new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID);

        assertThrows(VeiculoNaoEncontradoException.class,
                () -> criarOrdemDeServicoUseCase.criar(criarOrdemDeServicoDto));

        Mockito.verifyNoInteractions(ordemDeServicoGateway);
    }

    @Test
    void shouldThrowWhenClienteNotFound() {
        stubAtendente();
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculoPadrao()));
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.empty());
        CriarOrdemDeServicoDto criarOrdemDeServicoDto = new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID);

        assertThrows(ClienteNaoEncontradoException.class,
                () -> criarOrdemDeServicoUseCase.criar(criarOrdemDeServicoDto));

        Mockito.verifyNoInteractions(ordemDeServicoGateway);
    }

    @Test
    void shouldThrowWhenVeiculoNaoPertenceAoCliente() {
        stubAtendente();
        var veiculoDeOutroCliente = Veiculo.reconstituir(VEICULO_ID, 99L, "ABC1234", "Gol", 2020);
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculoDeOutroCliente));
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(clientePadrao()));
        CriarOrdemDeServicoDto criarOrdemDeServicoDto = new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID);

        assertThrows(VeiculoNaoPertenceAoClienteException.class,
                () -> criarOrdemDeServicoUseCase.criar(criarOrdemDeServicoDto));

        Mockito.verifyNoInteractions(ordemDeServicoGateway);
    }

    @Test
    void shouldPropagateExceptionFromGateway() {
        stubAtendente();
        stubClienteEVeiculo();
        Mockito.doThrow(new RuntimeException("erro no banco")).when(ordemDeServicoGateway).criar(Mockito.any());
        CriarOrdemDeServicoDto criarOrdemDeServicoDto = new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID);

        assertThrows(RuntimeException.class,
                () -> criarOrdemDeServicoUseCase.criar(criarOrdemDeServicoDto));
    }
}
