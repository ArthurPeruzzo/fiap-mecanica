package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.domain.Peca;
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
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.mensagem.MensagemOrdemDeServicoRecebidaFactory;
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
import com.fiap.mecanica.shared.metricas.core.gateway.MetricasGateway;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemParams;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CriarOrdemDeServicoUseCase {

	private final AtendenteGateway atendenteGateway;
	private final TokenGateway tokenGateway;
	private final OrdemDeServicoGateway ordemDeServicoGateway;
	private final VeiculoGateway veiculoGateway;
	private final ClienteGateway clienteGateway;
	private final ServicoGateway servicoGateway;
	private final PecaGateway pecaGateway;
	private final InsumoGateway insumoGateway;
	private final NotificacaoGateway notificacaoGateway;
	private final MetricasGateway metricasGateway;
	private final CriarOrdemDeServicoOutputPort outputPort;

	public CriarOrdemDeServicoUseCase(AtendenteGateway atendenteGateway,
									   TokenGateway tokenGateway,
									   OrdemDeServicoGateway ordemDeServicoGateway,
									   VeiculoGateway veiculoGateway,
									   ClienteGateway clienteGateway,
									   ServicoGateway servicoGateway,
									   PecaGateway pecaGateway,
									   InsumoGateway insumoGateway,
									   NotificacaoGateway notificacaoGateway,
									   MetricasGateway metricasGateway,
									   CriarOrdemDeServicoOutputPort outputPort) {
		this.atendenteGateway = atendenteGateway;
		this.tokenGateway = tokenGateway;
		this.ordemDeServicoGateway = ordemDeServicoGateway;
		this.veiculoGateway = veiculoGateway;
		this.clienteGateway = clienteGateway;
		this.servicoGateway = servicoGateway;
		this.pecaGateway = pecaGateway;
		this.insumoGateway = insumoGateway;
		this.notificacaoGateway = notificacaoGateway;
		this.metricasGateway = metricasGateway;
		this.outputPort = outputPort;
	}

	public void criar(CriarOrdemDeServicoDto dto) {
		Atendente atendente = buscaAtendentePorUsuarioId();
		Veiculo veiculo = buscaVeiculoPorId(dto.veiculoId());
		Cliente cliente = buscaClientePorId(dto.clienteId());

		validaVinculoVeiculoAndCliente(veiculo, cliente);
		validaOrdemAbertaParaVeiculo(veiculo);

		OrdemDeServico ordemDeServico = criaOrdemDeServico(dto, atendente);

		vincularServicos(ordemDeServico, dto.servicosIds());
		vincularPecas(ordemDeServico, dto.pecas());
		vincularInsumos(ordemDeServico, dto.insumos());

		notificarCliente(ordemDeServico);

		metricasGateway.registrarOrdemCriada();

		outputPort.apresentar(ordemDeServico.getId());
	}

	private Atendente buscaAtendentePorUsuarioId() {
		Long userId = tokenGateway.getUserId();
		return atendenteGateway.findByUsuarioId(userId).orElseThrow(AtendenteNaoEncontradoException::new);
	}

	private Veiculo buscaVeiculoPorId(Long veiculoId) {
		return veiculoGateway.buscarPorId(veiculoId).orElseThrow(VeiculoNaoEncontradoException::new);
	}

	private Cliente buscaClientePorId(Long clienteId) {
		return clienteGateway.buscarPorId(clienteId).orElseThrow(ClienteNaoEncontradoException::new);
	}

	private void validaVinculoVeiculoAndCliente(Veiculo veiculo, Cliente cliente) {
		if (!veiculo.pertenceAo(cliente.getId())) {
			throw new VeiculoNaoPertenceAoClienteException();
		}
	}

	private void validaOrdemAbertaParaVeiculo(Veiculo veiculo) {
		if (ordemDeServicoGateway.existeOrdemAbertaParaVeiculo(veiculo.getId())) {
			throw new OrdemDeServicoAbertaParaVeiculoException();
		}
	}

	private OrdemDeServico criaOrdemDeServico(CriarOrdemDeServicoDto dto, Atendente atendente) {
		OrdemDeServico ordemDeServico = new OrdemDeServico(dto.clienteId(), dto.veiculoId(), atendente.getId(), dto.descricao());
		Long ordemServicoId = ordemDeServicoGateway.criar(ordemDeServico);
		ordemDeServico.setId(ordemServicoId);
		return ordemDeServico;
	}

	private void vincularServicos(OrdemDeServico ordemDeServico, List<Long> servicosIds) {
		if (servicosIds == null || servicosIds.isEmpty()) return;

		List<Servico> servicos = servicoGateway.listarPorIds(servicosIds);
		if (servicos.size() != servicosIds.size()) {
			throw new ServicoNaoEncontradoException();
		}

		servicos.forEach(servico -> {
			ordemDeServico.vincularServico(servico.getId(), servico.getPreco());
			ordemDeServicoGateway.vincularServico(ordemDeServico.getId(), servico.getId(), servico.getPreco(), StatusServico.NAO_INICIADO);
		});
	}

	private void vincularPecas(OrdemDeServico ordemDeServico, List<PecaVinculadaCriarDto> pecas) {
		if (pecas == null || pecas.isEmpty()) return;

		List<Long> pecasIds = pecas.stream().map(PecaVinculadaCriarDto::id).toList();
		Map<Long, Peca> pecasPorId = pecaGateway.listarPorIds(pecasIds).stream()
				.collect(Collectors.toMap(Peca::getId, Function.identity()));

		if (pecasPorId.size() != pecasIds.size()) {
			throw new PecaNaoEncontradaException();
		}

		pecas.forEach(dto -> {
			Peca peca = pecasPorId.get(dto.id());
			peca.baixarEstoque(dto.quantidade());
			pecaGateway.atualizar(peca);
			ordemDeServico.vincularPeca(peca.getId(), dto.quantidade(), peca.getPreco());
			ordemDeServicoGateway.vincularOuSomarPeca(ordemDeServico.getId(), peca.getId(), dto.quantidade(), peca.getPreco());
		});
	}

	private void vincularInsumos(OrdemDeServico ordemDeServico, List<InsumoVinculadoCriarDto> insumos) {
		if (insumos == null || insumos.isEmpty()) return;

		List<Long> insumosIds = insumos.stream().map(InsumoVinculadoCriarDto::id).toList();
		Map<Long, Insumo> insumosPorId = insumoGateway.listarPorIds(insumosIds).stream()
				.collect(Collectors.toMap(Insumo::getId, Function.identity()));

		if (insumosPorId.size() != insumosIds.size()) {
			throw new InsumoNaoEncontradoException();
		}

		insumos.forEach(dto -> {
			Insumo insumo = insumosPorId.get(dto.id());
			insumo.baixarEstoque(dto.quantidade());
			insumoGateway.atualizar(insumo);
			ordemDeServico.vincularInsumo(insumo.getId(), dto.quantidade(), insumo.getPreco());
			ordemDeServicoGateway.vincularOuSomarInsumo(ordemDeServico.getId(), insumo.getId(), dto.quantidade(), insumo.getPreco());
		});
	}

	private void notificarCliente(OrdemDeServico ordemDeServico) {
		var params = MensagemParams.builder()
				.clienteId(ordemDeServico.getClienteId())
				.ordemId(ordemDeServico.getId())
				.build();
		notificacaoGateway.enviar(new MensagemOrdemDeServicoRecebidaFactory().criar(params));
	}
}
