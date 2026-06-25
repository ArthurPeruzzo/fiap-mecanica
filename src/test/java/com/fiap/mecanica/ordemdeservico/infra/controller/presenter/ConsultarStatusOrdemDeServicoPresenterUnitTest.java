package com.fiap.mecanica.ordemdeservico.infra.controller.presenter;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServicoStateFactory;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class ConsultarStatusOrdemDeServicoPresenterUnitTest {

    private static final Long ORDEM_ID = 1L;

    private OrdemDeServico ordemComStatus(StatusOrdemDeServico status) {
        return OrdemDeServico.builder()
                .id(ORDEM_ID)
                .clienteId(2L)
                .veiculoId(3L)
                .atendenteId(4L)
                .status(status)
                .state(OrdemDeServicoStateFactory.from(status))
                .build();
    }

    @ParameterizedTest
    @EnumSource(StatusOrdemDeServico.class)
    void shouldMapIdAndStatusForAllStatuses(StatusOrdemDeServico status) {
        var presenter = new ConsultarStatusOrdemDeServicoPresenter();

        presenter.apresentar(ordemComStatus(status));

        var viewModel = presenter.getViewModel();
        assertEquals(ORDEM_ID, viewModel.id());
        assertEquals(status.name(), viewModel.status());
    }

    @Test
    void shouldReturnNullViewModelBeforeApresentarIsCalled() {
        var presenter = new ConsultarStatusOrdemDeServicoPresenter();

        assertNull(presenter.getViewModel());
    }
}
