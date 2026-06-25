package com.fiap.mecanica.ordemdeservico.infra.controller.presenter;

import com.fiap.mecanica.ordemdeservico.core.dto.OrdemDeServicoListagemDto;
import com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico.ListarOrdemDeServicoOutputPort;
import com.fiap.mecanica.ordemdeservico.infra.controller.json.InsumoVinculadoResponseJson;
import com.fiap.mecanica.ordemdeservico.infra.controller.json.OrdemDeServicoResponseJson;
import com.fiap.mecanica.ordemdeservico.infra.controller.json.PecaVinculadaResponseJson;
import com.fiap.mecanica.ordemdeservico.infra.controller.json.ServicoVinculadoResponseJson;
import com.fiap.mecanica.ordemdeservico.infra.controller.json.TempoMedioExecucaoResponseJson;
import com.fiap.mecanica.shared.page.Pagina;

public class ListarOrdemDeServicoPresenter implements ListarOrdemDeServicoOutputPort {

    private Pagina<OrdemDeServicoResponseJson> viewModel;

    @Override
    public void apresentar(Pagina<OrdemDeServicoListagemDto> pagina) {
        this.viewModel = pagina.map(this::toResponseJson);
    }

    private OrdemDeServicoResponseJson toResponseJson(OrdemDeServicoListagemDto dto) {
        return OrdemDeServicoResponseJson.builder()
                .id(dto.getId())
                .nomeCliente(dto.getNomeCliente())
                .documentoCliente(dto.getDocumentoCliente())
                .veiculo(dto.getVeiculo())
                .nomeAtendente(dto.getNomeAtendente())
                .nomeMecanico(dto.getNomeMecanico())
                .status(dto.getStatus())
                .descricao(dto.getDescricao())
                .dataCriacao(dto.getDataCriacao())
                .dataInicioDiagnostico(dto.getDataInicioDiagnostico())
                .dataConclusaoDiagnostico(dto.getDataConclusaoDiagnostico())
                .servicos(dto.getServicos().stream()
                        .map(sv -> new ServicoVinculadoResponseJson(
                                sv.servicoId(), sv.preco(), sv.status(), sv.dataInicioExecucao(), sv.dataFimExecucao()))
                        .toList())
                .pecas(dto.getPecas().stream()
                        .map(pv -> new PecaVinculadaResponseJson(
                                pv.pecaId(), pv.quantidade(), pv.preco(), pv.valorTotal()))
                        .toList())
                .insumos(dto.getInsumos().stream()
                        .map(iv -> new InsumoVinculadoResponseJson(
                                iv.insumoId(), iv.quantidade(), iv.preco(), iv.valorTotal()))
                        .toList())
                .valorTotal(dto.getValorTotal())
                .tempoMedioExecucaoServicos(TempoMedioExecucaoResponseJson.from(dto.getTempoMedioExecucaoServicos()))
                .dataEnvioOrcamento(dto.getDataEnvioOrcamento())
                .dataCancelamento(dto.getDataCancelamento())
                .dataAprovacao(dto.getDataAprovacao())
                .dataFinalizacao(dto.getDataFinalizacao())
                .dataEntrega(dto.getDataEntrega())
                .build();
    }

    public Pagina<OrdemDeServicoResponseJson> getViewModel() {
        return viewModel;
    }
}
