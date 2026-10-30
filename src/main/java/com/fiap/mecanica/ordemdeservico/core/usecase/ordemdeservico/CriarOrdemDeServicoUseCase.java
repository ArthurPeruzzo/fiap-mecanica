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
import com.fiap.mecanica.ordemdeservico.core.dto.ItemReserva;
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
import com.fiap.mecanica.ordemdeservico.core.gateway.TransacaoGateway;
import com.fiap.mecanica.shared.metricas.core.gateway.MetricasGateway;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemParams;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class CriarOrdemDeServicoUseCase {

	private final AtendenteGateway atendenteGateway;
	private final TokenGateway tokenGateway;
	private final OrdemDeServicoGateway ordemDeServicoGateway;
	private final VeiculoGateway veiculoGateway;
	private final ClienteGateway clienteGateway;
	private final ServicoGateway servicoGateway;
	private final CatalogoEstoqueGateway catalogoEstoqueGateway;
	private final ReservaEstoqueGateway reservaEstoqueGateway;
	private final TransacaoGateway transacaoGateway;
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
									   TransacaoGateway transacaoGateway,
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
		this.transacaoGateway = transacaoGateway;
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

		OrdemDeServico ordemDeServico = new OrdemDeServico(dto.clienteId(), dto.veiculoId(), atendente.getId(), dto.descricao());

		List<Servico> servicos = buscaServicos(dto.servicosIds());
		Map<Long, ItemCatalogo> pecasPorId = buscaPecasNoCatalogo(dto.pecas());
		Map<Long, ItemCatalogo> insumosPorId = buscaInsumosNoCatalogo(dto.insumos());

		servicos.forEach(servico -> ordemDeServico.vincularServico(servico.getId(), servico.getPreco()));
		orEmpty(dto.pecas()).forEach(p -> ordemDeServico.vincularPeca(p.id(), p.quantidade(), pecasPorId.get(p.id()).preco()));
		orEmpty(dto.insumos()).forEach(i -> ordemDeServico.vincularInsumo(i.id(), i.quantidade(), insumosPorId.get(i.id()).preco()));

		persistirEReservarEstoque(ordemDeServico, servicos, dto.pecas(), pecasPorId, dto.insumos(), insumosPorId);

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

	private List<Servico> buscaServicos(List<Long> servicosIds) {
		if (servicosIds == null || servicosIds.isEmpty()) return List.of();

		List<Servico> servicos = servicoGateway.listarPorIds(servicosIds);
		if (servicos.size() != servicosIds.size()) {
			throw new ServicoNaoEncontradoException();
		}
		return servicos;
	}

	private Map<Long, ItemCatalogo> buscaPecasNoCatalogo(List<PecaVinculadaCriarDto> pecas) {
		if (pecas == null || pecas.isEmpty()) return Map.of();

		List<Long> pecasIds = pecas.stream().map(PecaVinculadaCriarDto::id).toList();
		Map<Long, ItemCatalogo> pecasPorId = catalogoEstoqueGateway.listarPecasPorIds(pecasIds).stream()
				.collect(Collectors.toMap(ItemCatalogo::id, Function.identity()));

		if (pecasPorId.size() != pecasIds.size()) {
			throw new PecaNaoEncontradaNoCatalogoException();
		}
		return pecasPorId;
	}

	private Map<Long, ItemCatalogo> buscaInsumosNoCatalogo(List<InsumoVinculadoCriarDto> insumos) {
		if (insumos == null || insumos.isEmpty()) return Map.of();

		List<Long> insumosIds = insumos.stream().map(InsumoVinculadoCriarDto::id).toList();
		Map<Long, ItemCatalogo> insumosPorId = catalogoEstoqueGateway.listarInsumosPorIds(insumosIds).stream()
				.collect(Collectors.toMap(ItemCatalogo::id, Function.identity()));

		if (insumosPorId.size() != insumosIds.size()) {
			throw new InsumoNaoEncontradoNoCatalogoException();
		}
		return insumosPorId;
	}

	/**
	 * Grava a OS e seus vínculos numa transação local e, como último passo dela, reserva todas as
	 * peças e insumos numa única chamada ao estoque (que é atômica do lado dele).
	 * <ul>
	 *   <li>Reserva rejeitada: a exceção desfaz a transação local; nada ficou reservado.</li>
	 *   <li>Reserva aceita mas falha depois (commit, ou resposta perdida por timeout): libera as reservas
	 *       da OS. É best-effort; se o estoque também não responder, a reserva órfã fica a cargo da expiração.</li>
	 * </ul>
	 */
	private void persistirEReservarEstoque(OrdemDeServico ordemDeServico,
										   List<Servico> servicos,
										   List<PecaVinculadaCriarDto> pecas, Map<Long, ItemCatalogo> pecasPorId,
										   List<InsumoVinculadoCriarDto> insumos, Map<Long, ItemCatalogo> insumosPorId) {
		AtomicBoolean reservaTentada = new AtomicBoolean(false);

		try {
			transacaoGateway.executar(() -> {
				Long ordemServicoId = ordemDeServicoGateway.criar(ordemDeServico);
				ordemDeServico.setId(ordemServicoId);

				servicos.forEach(servico -> ordemDeServicoGateway.vincularServico(
						ordemServicoId, servico.getId(), servico.getPreco(), StatusServico.NAO_INICIADO));
				orEmpty(pecas).forEach(p -> ordemDeServicoGateway.vincularOuSomarPeca(
						ordemServicoId, p.id(), p.quantidade(), pecasPorId.get(p.id()).preco()));
				orEmpty(insumos).forEach(i -> ordemDeServicoGateway.vincularOuSomarInsumo(
						ordemServicoId, i.id(), i.quantidade(), insumosPorId.get(i.id()).preco()));

				List<ItemReserva> reservas = montaReservas(ordemDeServico);
				if (!reservas.isEmpty()) {
					reservaTentada.set(true);
					reservaEstoqueGateway.reservarItens(ordemServicoId, reservas);
				}
			});
		} catch (RuntimeException e) {
			if (reservaTentada.get()) {
				liberarReservasSemFalhar(ordemDeServico.getId());
			}
			throw e;
		}
	}

	private List<ItemReserva> montaReservas(OrdemDeServico ordemDeServico) {
		List<ItemReserva> reservas = new ArrayList<>();
		ordemDeServico.getTotalQuantidadePecasMapeadasPorId()
				.forEach((pecaId, quantidade) -> reservas.add(ItemReserva.peca(pecaId, quantidade)));
		ordemDeServico.getTotalQuantidadeInsumosMapeadosPorId()
				.forEach((insumoId, quantidade) -> reservas.add(ItemReserva.insumo(insumoId, quantidade)));
		return reservas;
	}

	private void liberarReservasSemFalhar(Long ordemServicoId) {
		try {
			reservaEstoqueGateway.liberarReservas(ordemServicoId);
		} catch (RuntimeException e) {
			log.error("Nao foi possivel liberar as reservas da ordem {} apos falha na criacao; reserva orfa no estoque", ordemServicoId, e);
		}
	}

	private static <T> List<T> orEmpty(List<T> lista) {
		return lista == null ? List.of() : lista;
	}

	private void notificarCliente(OrdemDeServico ordemDeServico) {
		var params = MensagemParams.builder()
				.clienteId(ordemDeServico.getClienteId())
				.ordemId(ordemDeServico.getId())
				.build();
		notificacaoGateway.enviar(new MensagemOrdemDeServicoRecebidaFactory().criar(params));
	}
}
