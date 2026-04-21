package com.fiap.mecanica.gestao.infra.gateway.database;

import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import com.fiap.mecanica.gestao.infra.gateway.entity.ClienteEntity;
import com.fiap.mecanica.gestao.infra.gateway.entity.VeiculoEntity;
import com.fiap.mecanica.gestao.infra.gateway.repository.VeiculoRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import com.fiap.mecanica.shared.page.Pagina;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class VeiculoDatabaseGateway implements VeiculoGateway {

    private final VeiculoRepository veiculoRepository;

    @Override
    public void criar(Veiculo veiculo) {
        try {
            var entity = VeiculoEntity.builder()
                    .cliente(ClienteEntity.builder().id(veiculo.getClienteId()).build())
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
    public Optional<Veiculo> buscarPorId(Long id) {
        try {
            return veiculoRepository.findById(id)
                    .map(e -> Veiculo.reconstituir(e.getId(), e.getCliente().getId(), e.getPlaca(), e.getModelo(), e.getAno()));
        } catch (Exception e) {
            log.error("Erro ao buscar veiculo por id: {}", id, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public void atualizar(Veiculo veiculo) {
        try {
            var entity = VeiculoEntity.builder()
                    .id(veiculo.getId())
                    .cliente(ClienteEntity.builder().id(veiculo.getClienteId()).build())
                    .placa(veiculo.getPlaca().getValor())
                    .modelo(veiculo.getModelo())
                    .ano(veiculo.getAno())
                    .build();
            veiculoRepository.save(entity);
        } catch (Exception e) {
            log.error("Erro ao atualizar veiculo", e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public boolean existePorPlacaExcluindoId(String placa, Long id) {
        try {
            return veiculoRepository.existsByPlacaAndIdNot(placa, id);
        } catch (Exception e) {
            log.error("Erro ao verificar existência de veiculo por placa excluindo id", e);
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

    @Override
    public void deletar(Long id) {
        try {
            veiculoRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Erro ao deletar veiculo por id: {}", id, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public Pagina<Veiculo> listar(int page, int size) {
        try {
            var resultado = veiculoRepository.findAll(PageRequest.of(page, size));
            var veiculos = resultado.getContent().stream()
                    .map(e -> Veiculo.reconstituir(e.getId(), e.getCliente().getId(), e.getPlaca(), e.getModelo(), e.getAno()))
                    .toList();
            return new Pagina<>(veiculos, resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(), resultado.getTotalPages());
        } catch (Exception e) {
            log.error("Erro ao listar veiculos", e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }
}
