package com.fiap.mecanica.ordemdeservico.core.dto;

import java.util.List;

public record CriarOrdemDeServicoDto(
        Long clienteId,
        Long veiculoId,
        List<Long> servicosIds,
        List<PecaVinculadaCriarDto> pecas,
        List<InsumoVinculadoCriarDto> insumos,
        String descricao
) {
}
