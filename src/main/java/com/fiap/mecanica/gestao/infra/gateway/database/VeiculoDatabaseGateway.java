package com.fiap.mecanica.gestao.infra.gateway.database;

import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import com.fiap.mecanica.gestao.infra.gateway.entity.VeiculoEntity;
import com.fiap.mecanica.gestao.infra.gateway.repository.VeiculoRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class VeiculoDatabaseGateway implements VeiculoGateway {

    private final VeiculoRepository veiculoRepository;

    @Override
    public void criar(Veiculo veiculo) {
        try {
            var entity = VeiculoEntity.builder()
                    .clienteId(veiculo.getClienteId())
                    .placa(veiculo.getPlaca().getValor())
                    .modelo(veiculo.getModelo())
                    .ano(veiculo.getAno())
                    .build();
            veiculoRepository.save(entity);
        } catch (Exception e) {
            log.error("Erro ao criar veiculo", e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public boolean existePorPlaca(String placa) {
        try {
            return veiculoRepository.existsByPlaca(placa);
        } catch (Exception e) {
            log.error("Erro ao verificar existência de veiculo por placa", e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }
}
