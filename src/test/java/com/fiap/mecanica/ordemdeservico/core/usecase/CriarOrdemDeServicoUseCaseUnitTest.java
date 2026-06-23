package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.core.domain.UnidadeMedida;
import com.fiap.mecanica.estoque.core.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.mecanica.estoque.core.exception.PecaNaoEncontradaException;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.gestao.core.domain.Atendente;
import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.gestao.core.exception.AtendenteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.AtendenteGateway;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.dto.CriarOrdemDeServicoDto;
import com.fiap.mecanica.ordemdeservico.core.dto.InsumoVinculadoCriarDto;
import com.fiap.mecanica.ordemdeservico.core.dto.PecaVinculadaCriarDto;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoAbertaParaVeiculoException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.exception.VeiculoNaoPertenceAoClienteException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.CriarOrdemDeServicoUseCase;
import com.fiap.mecanica.shared.notificacao.core.domain.Mensagem;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
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

    @Mock
    private ServicoGateway servicoGateway;

    @Mock
    private PecaGateway pecaGateway;

    @Mock
    private InsumoGateway insumoGateway;

    @Mock
    private NotificacaoGateway notificacaoGateway;

    private static final Long USER_ID = 10L;
    private static final Long ATENDENTE_ID = 3L;
    private static final Long CLIENTE_ID = 1L;
    private static final Long VEICULO_ID = 2L;
    private static final String DESCRICAO = "Barulho ao frear";

    private Atendente atendentePadrao() {
        return Atendente.builder().id(ATENDENTE_ID).build();
    }

    private Cliente clientePadrao() {
        return Cliente.reconstituir(CLIENTE_ID, "Pedro", null, "12345678909");
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
        Mockito.when(ordemDeServicoGateway.existeOrdemAbertaParaVeiculo(VEICULO_ID)).thenReturn(false);
    }

    private CriarOrdemDeServicoDto dtoPadrao() {
        return new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID, null, null, null, DESCRICAO);
    }

    @Test
    void shouldCreateOrdemDeServicoWithCorrectFields() {
        stubAtendente();
        stubClienteEVeiculo();

        var captor = ArgumentCaptor.forClass(OrdemDeServico.class);

        criarOrdemDeServicoUseCase.criar(dtoPadrao());

        Mockito.verify(ordemDeServicoGateway).criar(captor.capture());
        Mockito.verify(notificacaoGateway).enviar(Mockito.any(Mensagem.class));
        var os = captor.getValue();

        assertEquals(CLIENTE_ID, os.getClienteId());
        assertEquals(VEICULO_ID, os.getVeiculoId());
        assertEquals(ATENDENTE_ID, os.getAtendenteId());
        assertEquals(DESCRICAO, os.getDescricao());
        assertEquals(StatusOrdemDeServico.RECEBIDA, os.getStatus());
        assertNotNull(os.getDataCriacao());
    }

    @Test
    void shouldThrowWhenAtendenteNotFound() {
        Mockito.when(tokenGateway.getUserId()).thenReturn(USER_ID);
        Mockito.when(atendenteGateway.findByUsuarioId(USER_ID)).thenReturn(Optional.empty());
        CriarOrdemDeServicoDto criarOrdemDeServicoDto = dtoPadrao();

        assertThrows(AtendenteNaoEncontradoException.class,
                () -> criarOrdemDeServicoUseCase.criar(criarOrdemDeServicoDto));

        Mockito.verifyNoInteractions(veiculoGateway, clienteGateway, ordemDeServicoGateway, notificacaoGateway);
    }

    @Test
    void shouldThrowWhenVeiculoNotFound() {
        stubAtendente();
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.empty());

        CriarOrdemDeServicoDto criarOrdemDeServicoDto = dtoPadrao();
        assertThrows(VeiculoNaoEncontradoException.class,
                () -> criarOrdemDeServicoUseCase.criar(criarOrdemDeServicoDto));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).criar(Mockito.any());
        Mockito.verifyNoInteractions(notificacaoGateway);
    }

    @Test
    void shouldThrowWhenClienteNotFound() {
        stubAtendente();
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculoPadrao()));
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.empty());

        CriarOrdemDeServicoDto criarOrdemDeServicoDto = dtoPadrao();
        assertThrows(ClienteNaoEncontradoException.class,
                () -> criarOrdemDeServicoUseCase.criar(criarOrdemDeServicoDto));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).criar(Mockito.any());
        Mockito.verifyNoInteractions(notificacaoGateway);
    }

    @Test
    void shouldThrowWhenVeiculoNaoPertenceAoCliente() {
        stubAtendente();
        var veiculoDeOutroCliente = Veiculo.reconstituir(VEICULO_ID, 99L, "ABC1234", "Gol", 2020);
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculoDeOutroCliente));
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(clientePadrao()));

        CriarOrdemDeServicoDto criarOrdemDeServicoDto = dtoPadrao();
        assertThrows(VeiculoNaoPertenceAoClienteException.class,
                () -> criarOrdemDeServicoUseCase.criar(criarOrdemDeServicoDto));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).criar(Mockito.any());
        Mockito.verifyNoInteractions(notificacaoGateway);
    }

    @Test
    void shouldThrowWhenOrdemAbertaExistsForVeiculo() {
        stubAtendente();
        Mockito.when(veiculoGateway.buscarPorId(VEICULO_ID)).thenReturn(Optional.of(veiculoPadrao()));
        Mockito.when(clienteGateway.buscarPorId(CLIENTE_ID)).thenReturn(Optional.of(clientePadrao()));
        Mockito.when(ordemDeServicoGateway.existeOrdemAbertaParaVeiculo(VEICULO_ID)).thenReturn(true);

        CriarOrdemDeServicoDto criarOrdemDeServicoDto = dtoPadrao();
        assertThrows(OrdemDeServicoAbertaParaVeiculoException.class,
                () -> criarOrdemDeServicoUseCase.criar(criarOrdemDeServicoDto));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).criar(Mockito.any());
        Mockito.verifyNoInteractions(notificacaoGateway);
    }

    @Test
    void shouldPropagateExceptionFromGateway() {
        stubAtendente();
        stubClienteEVeiculo();
        Mockito.doThrow(new RuntimeException("erro no banco")).when(ordemDeServicoGateway).criar(Mockito.any());
        CriarOrdemDeServicoDto criarOrdemDeServicoDto = dtoPadrao();
        assertThrows(RuntimeException.class,
                () -> criarOrdemDeServicoUseCase.criar(criarOrdemDeServicoDto));
    }

    @Test
    void shouldReturnOrdemId() {
        stubAtendente();
        stubClienteEVeiculo();
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(99L);

        var id = criarOrdemDeServicoUseCase.criar(dtoPadrao());

        assertEquals(99L, id);
    }

    @Test
    void shouldVincularServicosWhenProvided() {
        stubAtendente();
        stubClienteEVeiculo();
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(5L);
        var servico = Servico.reconstituir(10L, "Troca de óleo", "desc", BigDecimal.TEN);
        Mockito.when(servicoGateway.listarPorIds(List.of(10L))).thenReturn(List.of(servico));

        criarOrdemDeServicoUseCase.criar(new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID, List.of(10L), null, null, DESCRICAO));

        Mockito.verify(ordemDeServicoGateway).vincularServico(5L, 10L, BigDecimal.TEN, StatusServico.NAO_INICIADO);
    }

    @Test
    void shouldVincularPecasWhenProvided() {
        stubAtendente();
        stubClienteEVeiculo();
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(5L);
        var peca = Peca.reconstituir(20L, "Filtro", "desc", BigDecimal.TEN, 10);
        Mockito.when(pecaGateway.listarPorIds(List.of(20L))).thenReturn(List.of(peca));

        criarOrdemDeServicoUseCase.criar(new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID, null,
                List.of(new PecaVinculadaCriarDto(20L, 3)), null, DESCRICAO));

        Mockito.verify(pecaGateway).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway).vincularOuSomarPeca(5L, 20L, 3, BigDecimal.TEN);
    }

    @Test
    void shouldVincularInsumosWhenProvided() {
        stubAtendente();
        stubClienteEVeiculo();
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(5L);
        var insumo = Insumo.reconstituir(30L, "Óleo", "desc", BigDecimal.TEN, UnidadeMedida.LITRO, 10);
        Mockito.when(insumoGateway.listarPorIds(List.of(30L))).thenReturn(List.of(insumo));

        criarOrdemDeServicoUseCase.criar(new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID, null, null,
                List.of(new InsumoVinculadoCriarDto(30L, 4)), DESCRICAO));

        Mockito.verify(insumoGateway).atualizar(Mockito.any());
        Mockito.verify(ordemDeServicoGateway).vincularOuSomarInsumo(5L, 30L, 4, BigDecimal.TEN);
    }

    @Test
    void shouldThrowWhenServicoNotFoundOnCriar() {
        stubAtendente();
        stubClienteEVeiculo();
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(5L);
        Mockito.when(servicoGateway.listarPorIds(List.of(99L))).thenReturn(List.of());
        var dto = new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID, List.of(99L), null, null, DESCRICAO);
        assertThrows(ServicoNaoEncontradoException.class, () -> criarOrdemDeServicoUseCase.criar(dto));
    }

    @Test
    void shouldThrowWhenPecaNotFoundOnCriar() {
        stubAtendente();
        stubClienteEVeiculo();
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(5L);
        Mockito.when(pecaGateway.listarPorIds(List.of(99L))).thenReturn(List.of());
        CriarOrdemDeServicoDto dto = new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID, null,
                List.of(new PecaVinculadaCriarDto(99L, 1)), null, DESCRICAO);
        assertThrows(PecaNaoEncontradaException.class, () -> criarOrdemDeServicoUseCase.criar(dto));
    }

    @Test
    void shouldThrowWhenInsumoNotFoundOnCriar() {
        stubAtendente();
        stubClienteEVeiculo();
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(5L);
        Mockito.when(insumoGateway.listarPorIds(List.of(99L))).thenReturn(List.of());
        var dto = new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID, null, null,
                List.of(new InsumoVinculadoCriarDto(99L, 1)), DESCRICAO);
        assertThrows(InsumoNaoEncontradoException.class, () -> criarOrdemDeServicoUseCase.criar(dto));
    }

    @Test
    void shouldThrowWhenEstoqueInsuficienteOnCriarComPeca() {
        stubAtendente();
        stubClienteEVeiculo();
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(5L);
        var pecaSemEstoque = Peca.reconstituir(20L, "Filtro", "desc", BigDecimal.TEN, 0);
        Mockito.when(pecaGateway.listarPorIds(List.of(20L))).thenReturn(List.of(pecaSemEstoque));
        CriarOrdemDeServicoDto dto = new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID, null,
                List.of(new PecaVinculadaCriarDto(20L, 3)), null, DESCRICAO);
        assertThrows(EstoqueInsuficienteException.class, () -> criarOrdemDeServicoUseCase.criar(dto));
    }

    @Test
    void shouldThrowWhenEstoqueInsuficienteOnCriarComInsumo() {
        stubAtendente();
        stubClienteEVeiculo();
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(5L);
        var insumoSemEstoque = Insumo.reconstituir(30L, "Óleo", "desc", BigDecimal.TEN, UnidadeMedida.LITRO, 0);
        Mockito.when(insumoGateway.listarPorIds(List.of(30L))).thenReturn(List.of(insumoSemEstoque));

        CriarOrdemDeServicoDto dto = new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID, null, null,
                List.of(new InsumoVinculadoCriarDto(30L, 3)), DESCRICAO);
        assertThrows(EstoqueInsuficienteException.class, () -> criarOrdemDeServicoUseCase.criar(dto));
    }
}
