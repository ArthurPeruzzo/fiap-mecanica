package com.fiap.mecanica.ordemdeservico.infra.mensageria;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.gateway.EstoqueComandosGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.TransacaoGateway;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RespostasDaSagaListenerUnitTest {

    private static final Long ORDEM_ID = 1L;
    private static final String PAGAMENTO_CONFIRMADO = "pagamento.confirmado";
    private static final String PAGAMENTO_RECUSADO = "pagamento.recusado";
    private static final String BAIXA_CONFIRMADA = "baixa.confirmada";
    private static final String RESERVA_LIBERADA = "reserva.liberada";

    @Mock private OrdemDeServicoGateway ordemDeServicoGateway;
    @Mock private EstoqueComandosGateway estoqueComandosGateway;
    @Mock private TransacaoGateway transacaoGateway;
    @Mock private NotificacaoGateway notificacaoGateway;

    private RespostasDaSagaListener listener;

    @BeforeEach
    void setUp() {
        Mockito.lenient().doAnswer(i -> {
            ((Runnable) i.getArgument(0)).run();
            return null;
        }).when(transacaoGateway).executar(Mockito.any());
        listener = new RespostasDaSagaListener(ordemDeServicoGateway, estoqueComandosGateway, transacaoGateway,
                notificacaoGateway, PAGAMENTO_CONFIRMADO, PAGAMENTO_RECUSADO, BAIXA_CONFIRMADA, RESERVA_LIBERADA);
    }

    private OrdemDeServico ordem(StatusOrdemDeServico status) {
        return OrdemDeServico.builder()
                .id(ORDEM_ID).clienteId(10L).veiculoId(2L).atendenteId(3L).mecanicoId(5L)
                .status(status).descricao("Barulho ao frear")
                .dataCriacao(LocalDateTime.now())
                .servicosVinculados(new ArrayList<>(List.of(
                        new ServicoVinculado(10L, new BigDecimal("150.00"), StatusServico.NAO_INICIADO, null, null))))
                .pecasVinculadas(new ArrayList<>())
                .insumosVinculados(new ArrayList<>())
                .orcamento(new Orcamento(new BigDecimal("150.00")))
                .state(OrdemDeServicoStateFactory.from(status))
                .build();
    }

    private void osNoStatus(StatusOrdemDeServico status) {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordem(status)));
    }

    @Test
    void shouldPedirBaixaQuandoOPagamentoForConfirmado() {
        osNoStatus(StatusOrdemDeServico.AGUARDANDO_PAGAMENTO);

        listener.receber(new RespostaDaSagaMensagem(ORDEM_ID), PAGAMENTO_CONFIRMADO);

        Mockito.verify(estoqueComandosGateway).solicitarConfirmacaoDeBaixa(ORDEM_ID);
    }

    @Test
    void shouldCancelarELiberarReservasQuandoOPagamentoForRecusado() {
        osNoStatus(StatusOrdemDeServico.AGUARDANDO_PAGAMENTO);

        listener.receber(new RespostaDaSagaMensagem(ORDEM_ID), PAGAMENTO_RECUSADO);

        var captor = ArgumentCaptor.forClass(OrdemDeServico.class);
        Mockito.verify(ordemDeServicoGateway).atualizar(captor.capture());
        assertEquals(StatusOrdemDeServico.CANCELADA, captor.getValue().getStatus());
        Mockito.verify(estoqueComandosGateway).solicitarLiberacaoDeReservas(ORDEM_ID);
    }

    @Test
    void shouldIniciarExecucaoQuandoABaixaForConfirmada() {
        osNoStatus(StatusOrdemDeServico.AGUARDANDO_PAGAMENTO);

        listener.receber(new RespostaDaSagaMensagem(ORDEM_ID), BAIXA_CONFIRMADA);

        var captor = ArgumentCaptor.forClass(OrdemDeServico.class);
        Mockito.verify(ordemDeServicoGateway).atualizar(captor.capture());
        assertEquals(StatusOrdemDeServico.EM_EXECUCAO, captor.getValue().getStatus());
    }

    @Test
    void shouldApenasEncerrarASagaQuandoAsReservasForemLiberadas() {
        listener.receber(new RespostaDaSagaMensagem(ORDEM_ID), RESERVA_LIBERADA);

        Mockito.verifyNoInteractions(ordemDeServicoGateway, estoqueComandosGateway, notificacaoGateway);
    }

    @Test
    void shouldConfirmarSemEfeitoUmaRespostaRepetida() {
        osNoStatus(StatusOrdemDeServico.EM_EXECUCAO);

        assertDoesNotThrow(() -> listener.receber(new RespostaDaSagaMensagem(ORDEM_ID), BAIXA_CONFIRMADA));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
    }

    @Test
    void shouldRejeitarParaADlqMensagemSemOrdemServicoId() {
        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> listener.receber(new RespostaDaSagaMensagem(null), BAIXA_CONFIRMADA));
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.receber(null, BAIXA_CONFIRMADA));
    }

    @Test
    void shouldRejeitarParaADlqRoutingKeyDesconhecida() {
        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> listener.receber(new RespostaDaSagaMensagem(ORDEM_ID), "qualquer.coisa"));
    }
}
