package com.fiap.mecanica.ordemdeservico.core.gateway;

import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.shared.page.Pagina;

import java.util.Optional;

public interface ServicoGateway {
    void criar(Servico servico);
    Optional<Servico> buscarPorId(Long id);
    void atualizar(Servico servico);
    void deletar(Long id);
    Pagina<Servico> listar(int page, int size);
}
