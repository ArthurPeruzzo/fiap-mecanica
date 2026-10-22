package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

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
import com.fiap.mecanica.ordemdeservico.core.dto.ItemCatalogo;
import com.fiap.mecanica.ordemdeservico.core.dto.PecaVinculadaCriarDto;
import com.fiap.mecanica.ordemdeservico.core.exception.InsumoNaoEncontradoNoCatalogoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoAbertaParaVeiculoException;
import com.fiap.mecanica.ordemdeservico.core.exception.PecaNaoEncontradaNoCatalogoException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.exception.VeiculoNaoPertenceAoClienteException;
import com.fiap.mecanica.ordemdeservico.core.gateway.CatalogoEstoqueGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.ReservaEstoqueGateway;
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
	private final CatalogoEstoqueGateway catalogoEstoqueGateway;
	private final ReservaEstoqueGateway reservaEstoqueGateway;
	private final NotificacaoGateway notificacaoGateway;
	private final MetricasGateway metricasGateway;
	private final CriarOrdemDeServicoOutputPort outputPort;

	public CriarOrdemDeServicoUseCase(AtendenteGateway atendenteGateway,
									   TokenGateway tokenGateway,
									   OrdemDeServicoGateway ordemDeServicoGateway,
									   VeiculoGateway veiculoGateway,
									   ClienteGateway clienteGateway,
									   ServicoGateway servicoGateway,
									   CatalogoEstoqueGateway catalogoEstoqueGateway,
									   ReservaEstoqueGateway reservaEstoqueGateway,
									   NotificacaoGateway notificacaoGateway,
									   MetricasGateway metricasGateway,
									   CriarOrdemDeServicoOutputPort outputPort) {
		this.atendenteGateway = atendenteGateway;
		this.tokenGateway = tokenGateway;
		this.ordemDeServicoGateway = ordemDeServicoGateway;
		this.veiculoGateway = veiculoGateway;
		this.clienteGateway = clienteGateway;
		this.servicoGateway = servicoGateway;
		this.catalogoEstoqueGateway = catalogoEstoqueGateway;
		this.reservaEstoqueGateway = reservaEstoqueGateway;
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
		Map<Long, ItemCatalogo> pecasPorId = catalogoEstoqueGateway.listarPecasPorIds(pecasIds).stream()
				.collect(Collectors.toMap(ItemCatalogo::id, Function.identity()));

		if (pecasPorId.size() != pecasIds.size()) {
			throw new PecaNaoEncontradaNoCatalogoException();
		}

		pecas.forEach(dto -> {
			ItemCatalogo peca = pecasPorId.get(dto.id());
			ordemDeServico.vincularPeca(peca.id(), dto.quantidade(), peca.preco());
			reservaEstoqueGateway.reservarPeca(ordemDeServico.getId(), peca.id(), ordemDeServico.getTotalQuantidadePecasMapeadasPorId().get(peca.id()));
			ordemDeServicoGateway.vincularOuSomarPeca(ordemDeServico.getId(), peca.id(), dto.quantidade(), peca.preco());
		});
	}

	private void vincularInsumos(OrdemDeServico ordemDeServico, List<InsumoVinculadoCriarDto> insumos) {
		if (insumos == null || insumos.isEmpty()) return;

		List<Long> insumosIds = insumos.stream().map(InsumoVinculadoCriarDto::id).toList();
		Map<Long, ItemCatalogo> insumosPorId = catalogoEstoqueGateway.listarInsumosPorIds(insumosIds).stream()
				.collect(Collectors.toMap(ItemCatalogo::id, Function.identity()));

		if (insumosPorId.size() != insumosIds.size()) {
			throw new InsumoNaoEncontradoNoCatalogoException();
		}

		insumos.forEach(dto -> {
			ItemCatalogo insumo = insumosPorId.get(dto.id());
			ordemDeServico.vincularInsumo(insumo.id(), dto.quantidade(), insumo.preco());
			reservaEstoqueGateway.reservarInsumo(ordemDeServico.getId(), insumo.id(), ordemDeServico.getTotalQuantidadeInsumosMapeadosPorId().get(insumo.id()));
			ordemDeServicoGateway.vincularOuSomarInsumo(ordemDeServico.getId(), insumo.id(), dto.quantidade(), insumo.preco());
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
