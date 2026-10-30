package com.fiap.mecanica.ordemdeservico.core.gateway;

import com.fiap.mecanica.ordemdeservico.core.dto.ItemCatalogo;

import java.util.List;
import java.util.Optional;

public interface CatalogoEstoqueGateway {
    Optional<ItemCatalogo> buscarPeca(Long pecaId);
    Optional<ItemCatalogo> buscarInsumo(Long insumoId);
    List<ItemCatalogo> listarPecasPorIds(List<Long> pecasIds);
    List<ItemCatalogo> listarInsumosPorIds(List<Long> insumosIds);
}
