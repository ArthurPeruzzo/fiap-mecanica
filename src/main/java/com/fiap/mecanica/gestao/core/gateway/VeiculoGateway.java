package com.fiap.mecanica.gestao.core.gateway;

import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.shared.page.Pagina;

import java.util.Optional;

public interface VeiculoGateway {
    void criar(Veiculo veiculo);
    Optional<Veiculo> buscarPorId(Long id);
    void atualizar(Veiculo veiculo);
    boolean existePorPlaca(String placa);
    boolean existePorPlacaExcluindoId(String placa, Long id);
    Pagina<Veiculo> listar(int page, int size);
    void deletar(Long id);
}
