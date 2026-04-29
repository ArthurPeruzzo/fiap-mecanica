package com.fiap.mecanica.ordemdeservico.infra.gateway.database;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.InsumoVinculado;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.PecaVinculada;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoInsumoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoPecaEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.ServicoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OrdemDeServicoInsumoRepository;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OrdemDeServicoPecaRepository;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OrdemDeServicoRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrdemDeServicoDatabaseGateway implements OrdemDeServicoGateway {

    private final OrdemDeServicoRepository ordemDeServicoRepository;
    private final OrdemDeServicoPecaRepository ordemDeServicoPecaRepository;
    private final OrdemDeServicoInsumoRepository ordemDeServicoInsumoRepository;

    @Override
    public void criar(OrdemDeServico ordemDeServico) {
        try {
            ordemDeServicoRepository.save(OrdemDeServicoEntity.builder()
                    .clienteId(ordemDeServico.getClienteId())
                    .veiculoId(ordemDeServico.getVeiculoId())
                    .atendenteId(ordemDeServico.getAtendenteId())
                    .status(ordemDeServico.getStatus())
                    .descricao(ordemDeServico.getDescricao())
                    .dataCriacao(ordemDeServico.getDataCriacao())
                    .build());
        } catch (Exception e) {
            log.error("Erro ao criar ordem de servico", e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public Optional<OrdemDeServico> buscarPorId(Long id) {
        try {
            return ordemDeServicoRepository.findOrdemDeServicoById(id)
                    .map(entity -> {
                        var pecasVinculadas = ordemDeServicoPecaRepository.findByOrdemServicoId(id).stream()
                                .map(p -> new PecaVinculada(p.getPecaId(), p.getQuantidade()))
                                .toList();
                        var insumosVinculados = ordemDeServicoInsumoRepository.findByOrdemServicoId(id).stream()
                                .map(i -> new InsumoVinculado(i.getInsumoId(), i.getQuantidade()))
                                .toList();
                        return OrdemDeServico.reconstituir(
                                entity.getId(),
                                entity.getClienteId(),
                                entity.getVeiculoId(),
                                entity.getAtendenteId(),
                                entity.getMecanicoId(),
                                entity.getStatus(),
                                entity.getDescricao(),
                                entity.getDataCriacao(),
                                entity.getDataInicioDiagnostico(),
                                entity.getDataConclusaoDiagnostico(),
                                entity.getServicos().stream().map(ServicoEntity::getId).toList(),
                                pecasVinculadas,
                                insumosVinculados
                        );
                    });
        } catch (Exception e) {
            log.error("Erro ao buscar ordem de servico por id: {}", id, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public boolean existeOrdemAbertaParaVeiculo(Long veiculoId) {
        try {
            return ordemDeServicoRepository.existsByVeiculoIdAndStatusNotIn(
                    veiculoId,
                    List.of(StatusOrdemDeServico.FINALIZADA, StatusOrdemDeServico.ENTREGUE)
            );
        } catch (Exception e) {
            log.error("Erro ao verificar ordem aberta para veiculo id: {}", veiculoId, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public void atualizar(OrdemDeServico ordemDeServico) {
        try {
            ordemDeServicoRepository.atualizar(
                    ordemDeServico.getId(),
                    ordemDeServico.getMecanicoId(),
                    ordemDeServico.getStatus(),
                    ordemDeServico.getDataInicioDiagnostico(),
                    ordemDeServico.getDataConclusaoDiagnostico()
            );
        } catch (Exception e) {
            log.error("Erro ao atualizar ordem de servico id: {}", ordemDeServico.getId(), e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public void vincularServico(Long ordemServicoId, Long servicoId) {
        try {
            ordemDeServicoRepository.vincularServico(ordemServicoId, servicoId);
        } catch (Exception e) {
            log.error("Erro ao vincular servico {} na ordem de servico {}", servicoId, ordemServicoId, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public void desvincularServico(Long ordemServicoId, Long servicoId) {
        try {
            ordemDeServicoRepository.desvincularServico(ordemServicoId, servicoId);
        } catch (Exception e) {
            log.error("Erro ao desvincular servico {} da ordem de servico {}", servicoId, ordemServicoId, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public void vincularOuSomarPeca(Long ordemServicoId, Long pecaId, Integer quantidade) {
        try {
            ordemDeServicoPecaRepository.findByOrdemServicoIdAndPecaId(ordemServicoId, pecaId)
                    .ifPresentOrElse(
                            e -> ordemDeServicoPecaRepository.somarQuantidade(ordemServicoId, pecaId, quantidade),
                            () -> ordemDeServicoPecaRepository.save(
                                    OrdemDeServicoPecaEntity.builder()
                                            .ordemServicoId(ordemServicoId)
                                            .pecaId(pecaId)
                                            .quantidade(quantidade)
                                            .build()
                            )
                    );
        } catch (Exception e) {
            log.error("Erro ao vincular peca {} na ordem de servico {}", pecaId, ordemServicoId, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public void desvincularOuSubtrairPeca(Long ordemServicoId, Long pecaId, Integer quantidade) {
        try {
            ordemDeServicoPecaRepository.findByOrdemServicoIdAndPecaId(ordemServicoId, pecaId)
                    .ifPresent(entity -> {
                        if (entity.getQuantidade() - quantidade <= 0) {
                            ordemDeServicoPecaRepository.delete(entity);
                        } else {
                            ordemDeServicoPecaRepository.diminuirQuantidade(ordemServicoId, pecaId, quantidade);
                        }
                    });
        } catch (Exception e) {
            log.error("Erro ao desvincular peca {} na ordem de servico {}", pecaId, ordemServicoId, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public void vincularOuSomarInsumo(Long ordemServicoId, Long insumoId, Integer quantidade) {
        try {
            ordemDeServicoInsumoRepository.findByOrdemServicoIdAndInsumoId(ordemServicoId, insumoId)
                    .ifPresentOrElse(
                            e -> ordemDeServicoInsumoRepository.somarQuantidade(ordemServicoId, insumoId, quantidade),
                            () -> ordemDeServicoInsumoRepository.save(
                                    OrdemDeServicoInsumoEntity.builder()
                                            .ordemServicoId(ordemServicoId)
                                            .insumoId(insumoId)
                                            .quantidade(quantidade)
                                            .build()
                            )
                    );
        } catch (Exception e) {
            log.error("Erro ao vincular insumo {} na ordem de servico {}", insumoId, ordemServicoId, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public void desvincularOuSubtrairInsumo(Long ordemServicoId, Long insumoId, Integer quantidade) {
        try {
            ordemDeServicoInsumoRepository.findByOrdemServicoIdAndInsumoId(ordemServicoId, insumoId)
                    .ifPresent(entity -> {
                        if (entity.getQuantidade() - quantidade <= 0) {
                            ordemDeServicoInsumoRepository.delete(entity);
                        } else {
                            ordemDeServicoInsumoRepository.diminuirQuantidade(ordemServicoId, insumoId, quantidade);
                        }
                    });
        } catch (Exception e) {
            log.error("Erro ao desvincular insumo {} na ordem de servico {}", insumoId, ordemServicoId, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }
}
