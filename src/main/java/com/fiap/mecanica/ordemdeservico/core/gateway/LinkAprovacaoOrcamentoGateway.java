package com.fiap.mecanica.ordemdeservico.core.gateway;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.LinkAprovacaoOrcamento;

import java.util.Optional;

public interface LinkAprovacaoOrcamentoGateway {
    void salvar(LinkAprovacaoOrcamento link);
    Optional<LinkAprovacaoOrcamento> buscarPorToken(String token);
    void atualizar(LinkAprovacaoOrcamento link);
}
