package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.dto.ListarServicosDto;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
import com.fiap.mecanica.shared.page.Pagina;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListarServicosUseCase {

    private final ServicoGateway servicoGateway;

    public Pagina<Servico> listar(ListarServicosDto dto) {
        return servicoGateway.listar(dto.page(), dto.size());
    }
}
