package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.gestao.core.dto.ListarVeiculosDto;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import com.fiap.mecanica.shared.page.Pagina;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListarVeiculosUseCase {

    private final VeiculoGateway veiculoGateway;

    public Pagina<Veiculo> listar(ListarVeiculosDto dto) {
        return veiculoGateway.listar(dto.page(), dto.size());
    }
}
