package com.fiap.mecanica.ordemdeservico.infra.gateway.estoque;

import com.fiap.mecanica.ordemdeservico.core.dto.ItemReserva;
import com.fiap.mecanica.ordemdeservico.core.exception.EstoqueIndisponivelException;
import com.fiap.mecanica.ordemdeservico.core.exception.EstoqueInsuficienteParaOrdemException;
import com.fiap.mecanica.ordemdeservico.core.gateway.ReservaEstoqueGateway;
import com.fiap.mecanica.ordemdeservico.infra.gateway.estoque.json.ReservarItensEstoqueJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
@Slf4j
public class ReservaEstoqueHttpGateway implements ReservaEstoqueGateway {

	private static final int UNPROCESSABLE_ENTITY = 422;
	private static final String RESERVA_ORDEM = "/reserva/ordem-servico/{ordemServicoId}";

	private final RestClient restClient;

	public ReservaEstoqueHttpGateway(@Qualifier("estoqueRestClient") RestClient restClient) {
		this.restClient = restClient;
	}

	@Override
	public void reservarItens(Long ordemServicoId, List<ItemReserva> itens) {
		if (itens == null || itens.isEmpty()) return;

		var body = new ReservarItensEstoqueJson(itens.stream()
				.map(i -> new ReservarItensEstoqueJson.ItemJson(i.tipo().name(), i.itemId(), i.quantidadeTotal()))
				.toList());

		executar("reservar itens", ordemServicoId, () -> restClient.put()
				.uri(RESERVA_ORDEM + "/itens", ordemServicoId)
				.body(body)
				.retrieve()
				.toBodilessEntity());
	}

	@Override
	public void confirmarReservas(Long ordemServicoId) {
		executar("confirmar reservas", ordemServicoId, () -> restClient.post()
				.uri(RESERVA_ORDEM + "/confirmar", ordemServicoId)
				.retrieve()
				.toBodilessEntity());
	}

	@Override
	public void liberarReservas(Long ordemServicoId) {
		executar("liberar reservas", ordemServicoId, () -> restClient.post()
				.uri(RESERVA_ORDEM + "/liberar", ordemServicoId)
				.retrieve()
				.toBodilessEntity());
	}

	private void executar(String operacao, Long ordemServicoId, Runnable chamada) {
		try {
			chamada.run();
		} catch (HttpClientErrorException e) {
			if (e.getStatusCode().value() == UNPROCESSABLE_ENTITY) {
				throw new EstoqueInsuficienteParaOrdemException();
			}
			log.error("Estoque rejeitou a operação '{}' da ordem {}: {}", operacao, ordemServicoId, e.getStatusCode(), e);
			throw new EstoqueIndisponivelException();
		} catch (RestClientException e) {
			log.error("Falha ao chamar o estoque na operação '{}' da ordem {}", operacao, ordemServicoId, e);
			throw new EstoqueIndisponivelException();
		}
	}
}
