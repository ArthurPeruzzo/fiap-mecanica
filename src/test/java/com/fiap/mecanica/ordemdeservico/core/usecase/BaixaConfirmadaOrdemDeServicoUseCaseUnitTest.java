package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.*;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.*;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
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
class BaixaConfirmadaOrdemDeServicoUseCaseUnitTest {

    private static final Long ORDEM_ID = 1L;

    @Mock private OrdemDeServicoGateway ordemDeServicoGateway;

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

    @Test
    void shouldLevarAOsParaEmExecucao() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordem(StatusOrdemDeServico.AGUARDANDO_PAGAMENTO)));

        boolean aplicada = new BaixaConfirmadaOrdemDeServicoUseCase(ordemDeServicoGateway).executar(ORDEM_ID);

        assertTrue(aplicada);
        var captor = ArgumentCaptor.forClass(OrdemDeServico.class);
        Mockito.verify(ordemDeServicoGateway).atualizar(captor.capture());
        assertEquals(StatusOrdemDeServico.EM_EXECUCAO, captor.getValue().getStatus());
    }

    @Test
    void shouldIgnorarMensagemRepetida() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordem(StatusOrdemDeServico.EM_EXECUCAO)));

        assertFalse(new BaixaConfirmadaOrdemDeServicoUseCase(ordemDeServicoGateway).executar(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
    }

    @Test
    void shouldIgnorarQuandoAOsFoiCancelada() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.of(ordem(StatusOrdemDeServico.CANCELADA)));

        assertFalse(new BaixaConfirmadaOrdemDeServicoUseCase(ordemDeServicoGateway).executar(ORDEM_ID));

        Mockito.verify(ordemDeServicoGateway, Mockito.never()).atualizar(Mockito.any());
    }

    @Test
    void shouldLancarQuandoAOsNaoExiste() {
        Mockito.when(ordemDeServicoGateway.buscarPorId(ORDEM_ID)).thenReturn(Optional.empty());

        assertThrows(OrdemDeServicoNaoEncontradaException.class,
                () -> new BaixaConfirmadaOrdemDeServicoUseCase(ordemDeServicoGateway).executar(ORDEM_ID));
    }
}
