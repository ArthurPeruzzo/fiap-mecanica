package com.fiap.mecanica.ordemdeservico.core.domain;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.core.exception.TransicaoDeStatusInvalidaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/** Transições do estado AGUARDANDO_PAGAMENTO, o passo da OS durante a saga de pagamento. */
class OrdemDeServicoSagaPagamentoUnitTest {

    private OrdemDeServico ordem(StatusOrdemDeServico status) {
        return OrdemDeServico.builder()
                .id(1L).clienteId(10L).veiculoId(2L).atendenteId(3L).mecanicoId(5L)
                .status(status).descricao("Barulho ao frear")
                .servicosVinculados(new ArrayList<>())
                .pecasVinculadas(new ArrayList<>())
                .insumosVinculados(new ArrayList<>())
                .orcamento(new Orcamento(new BigDecimal("150.00")))
                .state(OrdemDeServicoStateFactory.from(status))
                .build();
    }

    @Test
    void aprovarLevaAOsParaAguardandoPagamentoEGravaADataDeAprovacao() {
        var os = ordem(StatusOrdemDeServico.AGUARDANDO_APROVACAO);

        os.aprovar();

        assertEquals(StatusOrdemDeServico.AGUARDANDO_PAGAMENTO, os.getStatus());
        assertNotNull(os.getDataAprovacao());
    }

    @Test
    void iniciarExecucaoLevaAguardandoPagamentoParaEmExecucao() {
        var os = ordem(StatusOrdemDeServico.AGUARDANDO_PAGAMENTO);

        os.iniciarExecucao();

        assertEquals(StatusOrdemDeServico.EM_EXECUCAO, os.getStatus());
    }

    @Test
    void pagamentoRecusadoLevaACanceladaEGravaADataDeCancelamento() {
        var os = ordem(StatusOrdemDeServico.AGUARDANDO_PAGAMENTO);

        os.cancelarPorPagamentoRecusado();

        assertEquals(StatusOrdemDeServico.CANCELADA, os.getStatus());
        assertNotNull(os.getDataCancelamento());
    }

    @Test
    void cancelarAManoNaoValeAguardandoPagamento() {
        var os = ordem(StatusOrdemDeServico.AGUARDANDO_PAGAMENTO);

        assertThrows(TransicaoDeStatusInvalidaException.class, os::cancelar);
        assertEquals(StatusOrdemDeServico.AGUARDANDO_PAGAMENTO, os.getStatus());
    }

    @Test
    void cancelarPorPagamentoRecusadoSoValeAguardandoPagamento() {
        for (var status : new StatusOrdemDeServico[]{StatusOrdemDeServico.AGUARDANDO_APROVACAO,
                StatusOrdemDeServico.EM_EXECUCAO, StatusOrdemDeServico.CANCELADA, StatusOrdemDeServico.FINALIZADA}) {
            assertThrows(TransicaoDeStatusInvalidaException.class, () -> ordem(status).cancelarPorPagamentoRecusado(), status.name());
        }
    }

    @Test
    void aguardandoPagamentoNaoPodeSerAprovadaDeNovoNemEntregue() {
        var os = ordem(StatusOrdemDeServico.AGUARDANDO_PAGAMENTO);

        assertThrows(TransicaoDeStatusInvalidaException.class, os::aprovar);
        assertThrows(TransicaoDeStatusInvalidaException.class, os::entregar);
    }

    @Test
    void iniciarExecucaoSoEValidoAguardandoPagamento() {
        for (var status : new StatusOrdemDeServico[]{StatusOrdemDeServico.AGUARDANDO_APROVACAO,
                StatusOrdemDeServico.EM_EXECUCAO, StatusOrdemDeServico.CANCELADA, StatusOrdemDeServico.FINALIZADA}) {
            assertThrows(TransicaoDeStatusInvalidaException.class, () -> ordem(status).iniciarExecucao(), status.name());
        }
    }
}
