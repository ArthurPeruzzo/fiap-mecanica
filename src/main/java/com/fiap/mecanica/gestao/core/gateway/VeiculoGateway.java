package com.fiap.mecanica.gestao.core.gateway;

import com.fiap.mecanica.gestao.core.domain.Veiculo;

public interface VeiculoGateway {
    void criar(Veiculo veiculo);
    boolean existePorPlaca(String placa);
}
