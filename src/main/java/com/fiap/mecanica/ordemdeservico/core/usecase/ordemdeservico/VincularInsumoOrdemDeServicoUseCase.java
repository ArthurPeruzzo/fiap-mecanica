package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.exception.InsumoNaoEncontradoException;
import com.fiap.mecanica.estoque.core.gateway.InsumoGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VincularInsumoOrdemDeServicoUseCase {
    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final InsumoGateway insumoGateway;

    public void vincular(Long ordemServicoId, Long insumoId, Integer quantidade) {
        OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);

        Insumo insumo = insumoGateway.buscarPorId(insumoId).orElseThrow(InsumoNaoEncontradoException::new);

        insumo.baixarEstoque(quantidade);
        ordemDeServico.vincularInsumo(insumoId, quantidade);

        insumoGateway.atualizar(insumo);
        ordemDeServicoGateway.vincularOuSomarInsumo(ordemServicoId, insumoId, quantidade);
    }
}
