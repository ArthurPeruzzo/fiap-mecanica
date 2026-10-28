package com.fiap.mecanica.ordemdeservico.core.dto;

/**
 * Quantidade total que a ordem de serviço quer ter reservada de um item do estoque.
 */
public record ItemReserva(Tipo tipo, Long itemId, Integer quantidadeTotal) {

	public enum Tipo {
		PECA,
		INSUMO
	}

	public static ItemReserva peca(Long pecaId, Integer quantidadeTotal) {
		return new ItemReserva(Tipo.PECA, pecaId, quantidadeTotal);
	}

	public static ItemReserva insumo(Long insumoId, Integer quantidadeTotal) {
		return new ItemReserva(Tipo.INSUMO, insumoId, quantidadeTotal);
	}
}
