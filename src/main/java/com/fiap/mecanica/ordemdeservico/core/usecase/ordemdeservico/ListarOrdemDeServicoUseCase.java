package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.gestao.core.exception.AtendenteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.exception.MecanicoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.AtendenteGateway;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.gestao.core.gateway.MecanicoGateway;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.dto.InsumoVinculadoDto;
import com.fiap.mecanica.ordemdeservico.core.dto.OrdemDeServicoListagemDto;
import com.fiap.mecanica.ordemdeservico.core.dto.PecaVinculadaDto;
import com.fiap.mecanica.ordemdeservico.core.dto.ServicoVinculadoDto;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;

public class ListarOrdemDeServicoUseCase {

	private final OrdemDeServicoGateway ordemDeServicoGateway;
	private final ClienteGateway clienteGateway;
	private final VeiculoGateway veiculoGateway;
	private final AtendenteGateway atendenteGateway;
	private final MecanicoGateway mecanicoGateway;
	private final ListarOrdemDeServicoOutputPort outputPort;

	public ListarOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
									    ClienteGateway clienteGateway,
									    VeiculoGateway veiculoGateway,
									    AtendenteGateway atendenteGateway,
									    MecanicoGateway mecanicoGateway,
									    ListarOrdemDeServicoOutputPort outputPort) {
		this.ordemDeServicoGateway = ordemDeServicoGateway;
		this.clienteGateway = clienteGateway;
		this.veiculoGateway = veiculoGateway;
		this.atendenteGateway = atendenteGateway;
		this.mecanicoGateway = mecanicoGateway;
		this.outputPort = outputPort;
	}

	public void listar(int page, int size) {
		outputPort.apresentar(ordemDeServicoGateway.listar(page, size).map(this::mapear));
	}

	private OrdemDeServicoListagemDto mapear(OrdemDeServico os) {
		var cliente = clienteGateway.buscarPorId(os.getClienteId()).orElseThrow(ClienteNaoEncontradoException::new);
		var veiculo = veiculoGateway.buscarPorId(os.getVeiculoId()).orElseThrow(VeiculoNaoEncontradoException::new);
		var atendente = atendenteGateway.findById(os.getAtendenteId()).orElseThrow(AtendenteNaoEncontradoException::new);

		var nomeMecanico = os.getMecanicoId() == null ? null
				: mecanicoGateway.findById(os.getMecanicoId())
						.orElseThrow(MecanicoNaoEncontradoException::new)
						.getNomeCompleto().nomeCompleto();

		var servicos = os.getServicosVinculados().stream()
				.map(sv -> new ServicoVinculadoDto(sv.servicoId(), sv.preco(), sv.status().name(), sv.dataInicioExecucao(), sv.dataFimExecucao()))
				.toList();

		var pecas = os.getPecasVinculadas().stream()
				.map(pv -> new PecaVinculadaDto(pv.pecaId(), pv.quantidade(), pv.preco(), pv.calculaValorTotal()))
				.toList();

		var insumos = os.getInsumosVinculados().stream()
				.map(iv -> new InsumoVinculadoDto(iv.insumoId(), iv.quantidade(), iv.preco(), iv.calculaValorTotal()))
				.toList();

		return OrdemDeServicoListagemDto.builder()
				.id(os.getId())
				.nomeCliente(cliente.getNome())
				.documentoCliente(cliente.getDocumento().getValorFormatado())
				.veiculo(veiculo.getInformacoesConcatenadas())
				.nomeAtendente(atendente.getNomeCompleto().nomeCompleto())
				.nomeMecanico(nomeMecanico)
				.status(os.getStatus().name())
				.descricao(os.getDescricao())
				.dataCriacao(os.getDataCriacao())
				.dataInicioDiagnostico(os.getDataInicioDiagnostico())
				.dataConclusaoDiagnostico(os.getDataConclusaoDiagnostico())
				.servicos(servicos)
				.pecas(pecas)
				.insumos(insumos)
				.valorTotal(os.getValorTotalOrcamento())
				.tempoMedioExecucaoServicos(os.calcularTempoMedioExecucaoServicos())
				.dataEnvioOrcamento(os.getDataEnvioOrcamento())
				.dataCancelamento(os.getDataCancelamento())
				.dataAprovacao(os.getDataAprovacao())
				.dataFinalizacao(os.getDataFinalizacao())
				.dataEntrega(os.getDataEntrega())
				.build();
	}
}
