package com.fiap.mecanica.ordemdeservico.infra.controller;

import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.estoque.core.gateway.PecaGateway;
import com.fiap.mecanica.gestao.core.gateway.AtendenteGateway;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.gestao.core.gateway.MecanicoGateway;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import com.fiap.mecanica.ordemdeservico.core.dto.CriarOrdemDeServicoDto;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.infra.controller.json.OrdemDeServicoResponseJson;
import com.fiap.mecanica.ordemdeservico.infra.controller.json.StatusOrdemDeServicoResponseJson;
import com.fiap.mecanica.ordemdeservico.infra.controller.presenter.ConsultarStatusOrdemDeServicoPresenter;
import com.fiap.mecanica.ordemdeservico.infra.controller.presenter.CriarOrdemDeServicoPresenter;
import com.fiap.mecanica.ordemdeservico.infra.controller.presenter.ListarOrdemDeServicoPresenter;
import com.fiap.mecanica.shared.metricas.core.gateway.MetricasGateway;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import com.fiap.mecanica.shared.page.Pagina;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.UserGateway;

public class OrdemDeServicoCleanController {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final AtendenteGateway atendenteGateway;
    private final TokenGateway tokenGateway;
    private final VeiculoGateway veiculoGateway;
    private final ClienteGateway clienteGateway;
    private final MecanicoGateway mecanicoGateway;
    private final ServicoGateway servicoGateway;
    private final PecaGateway pecaGateway;
    private final InsumoGateway insumoGateway;
    private final NotificacaoGateway notificacaoGateway;
    private final MetricasGateway metricasGateway;
    private final UserGateway userGateway;

    public OrdemDeServicoCleanController(OrdemDeServicoGateway ordemDeServicoGateway,
                                          AtendenteGateway atendenteGateway,
                                          TokenGateway tokenGateway,
                                          VeiculoGateway veiculoGateway,
                                          ClienteGateway clienteGateway,
                                          MecanicoGateway mecanicoGateway,
                                          ServicoGateway servicoGateway,
                                          PecaGateway pecaGateway,
                                          InsumoGateway insumoGateway,
                                          NotificacaoGateway notificacaoGateway,
                                          MetricasGateway metricasGateway,
                                          UserGateway userGateway) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.atendenteGateway = atendenteGateway;
        this.tokenGateway = tokenGateway;
        this.veiculoGateway = veiculoGateway;
        this.clienteGateway = clienteGateway;
        this.mecanicoGateway = mecanicoGateway;
        this.servicoGateway = servicoGateway;
        this.pecaGateway = pecaGateway;
        this.insumoGateway = insumoGateway;
        this.notificacaoGateway = notificacaoGateway;
        this.metricasGateway = metricasGateway;
        this.userGateway = userGateway;
    }

    public Long criar(CriarOrdemDeServicoDto dto) {
        var presenter = new CriarOrdemDeServicoPresenter();
        new CriarOrdemDeServicoUseCase(atendenteGateway, tokenGateway, ordemDeServicoGateway,
                veiculoGateway, clienteGateway, servicoGateway, pecaGateway, insumoGateway,
                notificacaoGateway, metricasGateway, presenter).criar(dto);
        return presenter.getViewModel();
    }

    public void entregar(Long ordemServicoId) {
        new EntregarOrdemDeServicoUseCase(ordemDeServicoGateway, notificacaoGateway, metricasGateway)
                .entregar(ordemServicoId);
    }

    public Pagina<OrdemDeServicoResponseJson> listar(int page, int size) {
        var presenter = new ListarOrdemDeServicoPresenter();
        new ListarOrdemDeServicoUseCase(ordemDeServicoGateway, clienteGateway, veiculoGateway,
                atendenteGateway, mecanicoGateway, presenter).listar(page, size);
        return presenter.getViewModel();
    }

    public StatusOrdemDeServicoResponseJson consultarStatus(Long ordemServicoId) {
        var presenter = new ConsultarStatusOrdemDeServicoPresenter();
        new ConsultarStatusOrdemDeServicoUseCase(ordemDeServicoGateway, presenter).consultar(ordemServicoId);
        return presenter.getViewModel();
    }

    public Pagina<OrdemDeServicoResponseJson> listarMinhasOrdens(int page, int size) {
        var presenter = new ListarOrdemDeServicoPresenter();
        new ListarOrdemDeServicoDoClienteUseCase(ordemDeServicoGateway, clienteGateway, veiculoGateway,
                atendenteGateway, mecanicoGateway, tokenGateway, userGateway, presenter).listar(page, size);
        return presenter.getViewModel();
    }
}
