package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.dto.ItemReserva;
import com.fiap.mecanica.ordemdeservico.core.exception.EstoqueInsuficienteParaOrdemException;
import com.fiap.mecanica.ordemdeservico.core.exception.InsumoNaoEncontradoNoCatalogoException;
import com.fiap.mecanica.ordemdeservico.core.exception.PecaNaoEncontradaNoCatalogoException;
import com.fiap.mecanica.ordemdeservico.core.dto.ItemCatalogo;
import com.fiap.mecanica.ordemdeservico.core.gateway.CatalogoEstoqueGateway;
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
import com.fiap.mecanica.ordemdeservico.core.gateway.ReservaEstoqueGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.TransacaoGateway;
import com.fiap.mecanica.ordemdeservico.core.exception.EstoqueIndisponivelException;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.CriarOrdemDeServicoOutputPort;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.CriarOrdemDeServicoUseCase;
import com.fiap.mecanica.shared.metricas.core.gateway.MetricasGateway;
import com.fiap.mecanica.shared.notificacao.core.domain.Mensagem;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CriarOrdemDeServicoUseCaseUnitTest {

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
    private CatalogoEstoqueGateway catalogoEstoqueGateway;
    @Mock
    private ReservaEstoqueGateway reservaEstoqueGateway;

    @Mock
    private TransacaoGateway transacaoGateway;

    @Mock
    private NotificacaoGateway notificacaoGateway;

    @Mock
    private MetricasGateway metricasGateway;

    @Mock
    private CriarOrdemDeServicoOutputPort outputPort;

    @BeforeEach
    void setUp() {
        criarOrdemDeServicoUseCase = new CriarOrdemDeServicoUseCase(
                atendenteGateway, tokenGateway, ordemDeServicoGateway,
                veiculoGateway, clienteGateway, servicoGateway, catalogoEstoqueGateway,
                reservaEstoqueGateway, transacaoGateway, notificacaoGateway, metricasGateway, outputPort);
        Mockito.lenient().doAnswer(invocation -> {
            invocation.<Runnable>getArgument(0).run();
            return null;
        }).when(transacaoGateway).executar(Mockito.any());
    }

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
        Mockito.verify(metricasGateway).registrarOrdemCriada();
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

        Mockito.verifyNoInteractions(veiculoGateway, clienteGateway, ordemDeServicoGateway, notificacaoGateway, metricasGateway);
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
        Mockito.verifyNoInteractions(metricasGateway);
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
        Mockito.verifyNoInteractions(metricasGateway);
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
        Mockito.verifyNoInteractions(metricasGateway);
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
        Mockito.verifyNoInteractions(metricasGateway);
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

        criarOrdemDeServicoUseCase.criar(dtoPadrao());

        Mockito.verify(outputPort).apresentar(99L);
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
        var peca = new ItemCatalogo(20L, BigDecimal.TEN);
        Mockito.when(catalogoEstoqueGateway.listarPecasPorIds(List.of(20L))).thenReturn(List.of(peca));

        criarOrdemDeServicoUseCase.criar(new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID, null,
                List.of(new PecaVinculadaCriarDto(20L, 3)), null, DESCRICAO));

        Mockito.verify(reservaEstoqueGateway).reservarItens(5L, List.of(ItemReserva.peca(20L, 3)));
        Mockito.verify(ordemDeServicoGateway).vincularOuSomarPeca(5L, 20L, 3, BigDecimal.TEN);
    }

    @Test
    void shouldVincularInsumosWhenProvided() {
        stubAtendente();
        stubClienteEVeiculo();
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(5L);
        var insumo = new ItemCatalogo(30L, BigDecimal.TEN);
        Mockito.when(catalogoEstoqueGateway.listarInsumosPorIds(List.of(30L))).thenReturn(List.of(insumo));

        criarOrdemDeServicoUseCase.criar(new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID, null, null,
                List.of(new InsumoVinculadoCriarDto(30L, 4)), DESCRICAO));

        Mockito.verify(reservaEstoqueGateway).reservarItens(5L, List.of(ItemReserva.insumo(30L, 4)));
        Mockito.verify(ordemDeServicoGateway).vincularOuSomarInsumo(5L, 30L, 4, BigDecimal.TEN);
    }

    @Test
    void shouldThrowWhenServicoNotFoundOnCriar() {
        stubAtendente();
        stubClienteEVeiculo();
        Mockito.when(servicoGateway.listarPorIds(List.of(99L))).thenReturn(List.of());
        var dto = new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID, List.of(99L), null, null, DESCRICAO);
        assertThrows(ServicoNaoEncontradoException.class, () -> criarOrdemDeServicoUseCase.criar(dto));
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).criar(Mockito.any());
    }

    @Test
    void shouldThrowWhenPecaNotFoundOnCriar() {
        stubAtendente();
        stubClienteEVeiculo();
        Mockito.when(catalogoEstoqueGateway.listarPecasPorIds(List.of(99L))).thenReturn(List.of());
        CriarOrdemDeServicoDto dto = new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID, null,
                List.of(new PecaVinculadaCriarDto(99L, 1)), null, DESCRICAO);
        assertThrows(PecaNaoEncontradaNoCatalogoException.class, () -> criarOrdemDeServicoUseCase.criar(dto));
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).criar(Mockito.any());
        Mockito.verifyNoInteractions(reservaEstoqueGateway);
    }

    @Test
    void shouldThrowWhenInsumoNotFoundOnCriar() {
        stubAtendente();
        stubClienteEVeiculo();
        Mockito.when(catalogoEstoqueGateway.listarInsumosPorIds(List.of(99L))).thenReturn(List.of());
        var dto = new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID, null, null,
                List.of(new InsumoVinculadoCriarDto(99L, 1)), DESCRICAO);
        assertThrows(InsumoNaoEncontradoNoCatalogoException.class, () -> criarOrdemDeServicoUseCase.criar(dto));
        Mockito.verify(ordemDeServicoGateway, Mockito.never()).criar(Mockito.any());
        Mockito.verifyNoInteractions(reservaEstoqueGateway);
    }

    @Test
    void shouldThrowWhenEstoqueInsuficienteOnCriarComPeca() {
        stubAtendente();
        stubClienteEVeiculo();
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(5L);
        var pecaSemEstoque = new ItemCatalogo(20L, BigDecimal.TEN);
        Mockito.when(catalogoEstoqueGateway.listarPecasPorIds(List.of(20L))).thenReturn(List.of(pecaSemEstoque));
        Mockito.doThrow(new EstoqueInsuficienteParaOrdemException()).when(reservaEstoqueGateway).reservarItens(5L, List.of(ItemReserva.peca(20L, 3)));
        CriarOrdemDeServicoDto dto = new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID, null,
                List.of(new PecaVinculadaCriarDto(20L, 3)), null, DESCRICAO);
        assertThrows(EstoqueInsuficienteParaOrdemException.class, () -> criarOrdemDeServicoUseCase.criar(dto));
    }

    @Test
    void shouldThrowWhenEstoqueInsuficienteOnCriarComInsumo() {
        stubAtendente();
        stubClienteEVeiculo();
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(5L);
        var insumoSemEstoque = new ItemCatalogo(30L, BigDecimal.TEN);
        Mockito.when(catalogoEstoqueGateway.listarInsumosPorIds(List.of(30L))).thenReturn(List.of(insumoSemEstoque));
        Mockito.doThrow(new EstoqueInsuficienteParaOrdemException()).when(reservaEstoqueGateway).reservarItens(5L, List.of(ItemReserva.insumo(30L, 3)));

        CriarOrdemDeServicoDto dto = new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID, null, null,
                List.of(new InsumoVinculadoCriarDto(30L, 3)), DESCRICAO);
        assertThrows(EstoqueInsuficienteParaOrdemException.class, () -> criarOrdemDeServicoUseCase.criar(dto));
    }

    // ---- reserva de estoque em lote + compensação ----

    private CriarOrdemDeServicoDto dtoComPecaEInsumo() {
        return new CriarOrdemDeServicoDto(CLIENTE_ID, VEICULO_ID, null,
                List.of(new PecaVinculadaCriarDto(20L, 3)), List.of(new InsumoVinculadoCriarDto(30L, 4)), DESCRICAO);
    }

    private void stubCatalogoPecaEInsumo() {
        Mockito.when(catalogoEstoqueGateway.listarPecasPorIds(List.of(20L))).thenReturn(List.of(new ItemCatalogo(20L, BigDecimal.TEN)));
        Mockito.when(catalogoEstoqueGateway.listarInsumosPorIds(List.of(30L))).thenReturn(List.of(new ItemCatalogo(30L, BigDecimal.ONE)));
    }

    @Test
    void shouldReservarPecasEInsumosNumaUnicaChamadaAoEstoque() {
        stubAtendente();
        stubClienteEVeiculo();
        stubCatalogoPecaEInsumo();
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(5L);

        criarOrdemDeServicoUseCase.criar(dtoComPecaEInsumo());

        var captor = ArgumentCaptor.<List<ItemReserva>>captor();
        Mockito.verify(reservaEstoqueGateway, Mockito.times(1)).reservarItens(Mockito.eq(5L), captor.capture());
        assertEquals(2, captor.getValue().size());
        assertTrue(captor.getValue().contains(ItemReserva.peca(20L, 3)));
        assertTrue(captor.getValue().contains(ItemReserva.insumo(30L, 4)));
        Mockito.verify(reservaEstoqueGateway, Mockito.never()).liberarReservas(Mockito.any());
    }

    @Test
    void shouldReservarEstoqueComoUltimoPassoDaTransacao() {
        stubAtendente();
        stubClienteEVeiculo();
        stubCatalogoPecaEInsumo();
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(5L);

        criarOrdemDeServicoUseCase.criar(dtoComPecaEInsumo());

        var ordem = Mockito.inOrder(transacaoGateway, ordemDeServicoGateway, reservaEstoqueGateway, notificacaoGateway);
        ordem.verify(transacaoGateway).executar(Mockito.any());
        ordem.verify(ordemDeServicoGateway).criar(Mockito.any());
        ordem.verify(ordemDeServicoGateway).vincularOuSomarPeca(5L, 20L, 3, BigDecimal.TEN);
        ordem.verify(ordemDeServicoGateway).vincularOuSomarInsumo(5L, 30L, 4, BigDecimal.ONE);
        ordem.verify(reservaEstoqueGateway).reservarItens(Mockito.eq(5L), Mockito.anyList());
        ordem.verify(notificacaoGateway).enviar(Mockito.any(Mensagem.class));
    }

    @Test
    void shouldNaoChamarEstoqueQuandoNaoHaPecasNemInsumos() {
        stubAtendente();
        stubClienteEVeiculo();
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(5L);

        criarOrdemDeServicoUseCase.criar(dtoPadrao());

        Mockito.verifyNoInteractions(reservaEstoqueGateway);
    }

    @Test
    void shouldFalharSemNotificarQuandoEstoqueIndisponivel() {
        stubAtendente();
        stubClienteEVeiculo();
        stubCatalogoPecaEInsumo();
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(5L);
        Mockito.doThrow(new EstoqueIndisponivelException())
                .when(reservaEstoqueGateway).reservarItens(Mockito.eq(5L), Mockito.anyList());
        var dto = dtoComPecaEInsumo();

        assertThrows(EstoqueIndisponivelException.class, () -> criarOrdemDeServicoUseCase.criar(dto));

        Mockito.verifyNoInteractions(notificacaoGateway, metricasGateway, outputPort);
    }

    @Test
    void shouldLiberarReservasQuandoReservaFalhaSemSaberSeFoiAplicada() {
        stubAtendente();
        stubClienteEVeiculo();
        stubCatalogoPecaEInsumo();
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(5L);
        // timeout de leitura: o estoque pode ter reservado e a resposta se perdeu
        Mockito.doThrow(new EstoqueIndisponivelException())
                .when(reservaEstoqueGateway).reservarItens(Mockito.eq(5L), Mockito.anyList());
        var dto = dtoComPecaEInsumo();

        assertThrows(EstoqueIndisponivelException.class, () -> criarOrdemDeServicoUseCase.criar(dto));

        Mockito.verify(reservaEstoqueGateway).liberarReservas(5L);
    }

    @Test
    void shouldLiberarReservasQuandoCommitDaTransacaoFalhaDepoisDaReserva() {
        stubAtendente();
        stubClienteEVeiculo();
        stubCatalogoPecaEInsumo();
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(5L);
        Mockito.doAnswer(invocation -> {
            invocation.<Runnable>getArgument(0).run();
            throw new IllegalStateException("commit falhou");
        }).when(transacaoGateway).executar(Mockito.any());
        var dto = dtoComPecaEInsumo();

        assertThrows(IllegalStateException.class, () -> criarOrdemDeServicoUseCase.criar(dto));

        Mockito.verify(reservaEstoqueGateway).reservarItens(Mockito.eq(5L), Mockito.anyList());
        Mockito.verify(reservaEstoqueGateway).liberarReservas(5L);
        Mockito.verifyNoInteractions(notificacaoGateway);
    }

    @Test
    void shouldPropagarOErroOriginalMesmoQuandoALiberacaoFalha() {
        stubAtendente();
        stubClienteEVeiculo();
        stubCatalogoPecaEInsumo();
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(5L);
        Mockito.doThrow(new EstoqueIndisponivelException())
                .when(reservaEstoqueGateway).reservarItens(Mockito.eq(5L), Mockito.anyList());
        Mockito.doThrow(new EstoqueIndisponivelException()).when(reservaEstoqueGateway).liberarReservas(5L);
        var dto = dtoComPecaEInsumo();

        assertThrows(EstoqueIndisponivelException.class, () -> criarOrdemDeServicoUseCase.criar(dto));
    }

    @Test
    void shouldNaoLiberarReservasQuandoFalhaAntesDeReservar() {
        stubAtendente();
        stubClienteEVeiculo();
        stubCatalogoPecaEInsumo();
        Mockito.when(ordemDeServicoGateway.criar(Mockito.any())).thenReturn(5L);
        Mockito.doThrow(new RuntimeException("erro no banco"))
                .when(ordemDeServicoGateway).vincularOuSomarPeca(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.any());
        var dto = dtoComPecaEInsumo();

        assertThrows(RuntimeException.class, () -> criarOrdemDeServicoUseCase.criar(dto));

        Mockito.verifyNoInteractions(reservaEstoqueGateway);
    }
}
