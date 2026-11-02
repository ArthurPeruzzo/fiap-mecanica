package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.dto.ItemReserva;
import com.fiap.mecanica.ordemdeservico.core.exception.EstoqueInsuficienteParaOrdemException;
import com.fiap.mecanica.ordemdeservico.core.gateway.ReservaEstoqueGateway;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Reserva um item no estoque e, em seguida, grava o vínculo local. Como a reserva é absoluta e
 * idempotente (total por item), uma falha depois dela é compensada reservando de novo o total anterior.
 * <ul>
 *   <li>Estoque insuficiente: o estoque rejeita de forma atômica, nada a restaurar.</li>
 *   <li>Outra falha na reserva (ex.: timeout, sem saber se foi aplicada) ou falha na gravação local: restaura
 *       o total anterior. É best-effort; se o estoque também não responder, a divergência fica registrada no
 *       log e é corrigida na próxima alteração do mesmo item.</li>
 * </ul>
 */
@Slf4j
final class ReservaComCompensacao {

	private ReservaComCompensacao() {
	}

	static void executar(ReservaEstoqueGateway reservaEstoqueGateway, Long ordemServicoId,
						 ItemReserva novoTotal, ItemReserva totalAnterior, Runnable persistirVinculo) {
		try {
			reservaEstoqueGateway.reservarItens(ordemServicoId, List.of(novoTotal));
			persistirVinculo.run();
		} catch (EstoqueInsuficienteParaOrdemException e) {
			throw e;
		} catch (RuntimeException e) {
			restaurarSemFalhar(reservaEstoqueGateway, ordemServicoId, totalAnterior);
			throw e;
		}
	}

	private static void restaurarSemFalhar(ReservaEstoqueGateway reservaEstoqueGateway, Long ordemServicoId,
										   ItemReserva totalAnterior) {
		try {
			reservaEstoqueGateway.reservarItens(ordemServicoId, List.of(totalAnterior));
		} catch (RuntimeException e) {
			log.error("Nao foi possivel restaurar a reserva de {} {} da ordem {} para {} apos falha; reserva divergente no estoque",
					totalAnterior.tipo(), totalAnterior.itemId(), ordemServicoId, totalAnterior.quantidadeTotal(), e);
		}
	}
}
