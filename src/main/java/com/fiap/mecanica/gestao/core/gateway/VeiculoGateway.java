package com.fiap.mecanica.gestao.core.gateway;

import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.shared.page.Pagina;

public interface VeiculoGateway {
    void criar(Veiculo veiculo);
    boolean existePorPlaca(String placa);
    Pagina<Veiculo> listar(int page, int size);
}
