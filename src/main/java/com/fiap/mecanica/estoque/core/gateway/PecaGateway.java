package com.fiap.mecanica.estoque.core.gateway;

import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.shared.page.Pagina;

import java.util.Optional;

public interface PecaGateway {
    void criar(Peca peca);
    Optional<Peca> buscarPorId(Long id);
    void atualizar(Peca peca);
    void deletar(Long id);
    Pagina<Peca> listar(int page, int size);
}
