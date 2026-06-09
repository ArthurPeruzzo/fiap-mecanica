package com.fiap.mecanica.ordemdeservico.infra.gateway.database;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.*;
import com.fiap.mecanica.ordemdeservico.core.domain.servico.StatusServico;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoInsumoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoPecaEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoServicoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OrdemDeServicoInsumoRepository;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OrdemDeServicoPecaRepository;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OrdemDeServicoRepository;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OrdemDeServicoServicoRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import com.fiap.mecanica.shared.page.Pagina;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrdemDeServicoDatabaseGateway implements OrdemDeServicoGateway {

    private final OrdemDeServicoRepository ordemDeServicoRepository;
    private final OrdemDeServicoPecaRepository ordemDeServicoPecaRepository;
    private final OrdemDeServicoInsumoRepository ordemDeServicoInsumoRepository;
    private final OrdemDeServicoServicoRepository ordemDeServicoServicoRepository;

    @Override
    public Long criar(OrdemDeServico ordemDeServico) {
        try {
            OrdemDeServicoEntity entity = ordemDeServicoRepository.save(OrdemDeServicoEntity.builder()
                    .clienteId(ordemDeServico.getClienteId())
                    .veiculoId(ordemDeServico.getVeiculoId())
                    .atendenteId(ordemDeServico.getAtendenteId())
                    .status(ordemDeServico.getStatus())
                    .descricao(ordemDeServico.getDescricao())
                    .dataCriacao(ordemDeServico.getDataCriacao())
                    .build());
            return entity.getId();
        } catch (Exception e) {
            log.error("Erro ao criar ordem de servico", e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public Optional<OrdemDeServico> buscarPorId(Long id) {
        try {
            return ordemDeServicoRepository.findById(id).map(this::mapear);
        } catch (Exception e) {
            log.error("Erro ao buscar ordem de servico por id: {}", id, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    private OrdemDeServico mapear(OrdemDeServicoEntity entity) {
        Long id = entity.getId();

        var servicosVinculados = ordemDeServicoServicoRepository.findByOrdemServicoId(id).stream()
                .map(s -> new ServicoVinculado(s.getServicoId(), s.getPreco(), s.getStatus(), s.getDataInicioExecucao(), s.getDataFimExecucao()))
                .toList();

        var pecasVinculadas = ordemDeServicoPecaRepository.findByOrdemServicoId(id).stream()
                .map(p -> new PecaVinculada(p.getPecaId(), p.getQuantidade(), p.getPreco()))
                .toList();

        var insumosVinculados = ordemDeServicoInsumoRepository.findByOrdemServicoId(id).stream()
                .map(i -> new InsumoVinculado(i.getInsumoId(), i.getQuantidade(), i.getPreco()))
                .toList();

        var orcamento = entity.getOrcamentoTotal() != null ? new Orcamento(entity.getOrcamentoTotal()) : null;

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
                servicosVinculados,
                pecasVinculadas,
                insumosVinculados,
                orcamento,
                entity.getDataEnvioOrcamento(),
                entity.getDataCancelamento(),
                entity.getDataAprovacao(),
                entity.getDataFinalizacao(),
                entity.getDataEntrega()
        );
    }

    @Override
    public boolean existeOrdemAbertaParaVeiculo(Long veiculoId) {
        try {
            return ordemDeServicoRepository.existsByVeiculoIdAndStatusNotIn(
                    veiculoId,
                    List.of(StatusOrdemDeServico.FINALIZADA, StatusOrdemDeServico.ENTREGUE, StatusOrdemDeServico.CANCELADA)
            );
        } catch (Exception e) {
            log.error("Erro ao verificar ordem aberta para veiculo id: {}", veiculoId, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public void atualizar(OrdemDeServico ordemDeServico) {
        try {
            var orcamentoTotal = ordemDeServico.getOrcamento() != null
                    ? ordemDeServico.getOrcamento().valorTotal() : null;
            ordemDeServicoRepository.atualizar(
                    ordemDeServico.getId(),
                    ordemDeServico.getMecanicoId(),
                    ordemDeServico.getStatus(),
                    ordemDeServico.getDataInicioDiagnostico(),
                    ordemDeServico.getDataConclusaoDiagnostico(),
                    orcamentoTotal,
                    ordemDeServico.getDataEnvioOrcamento(),
                    ordemDeServico.getDataCancelamento(),
                    ordemDeServico.getDataAprovacao(),
                    ordemDeServico.getDataFinalizacao(),
                    ordemDeServico.getDataEntrega()
            );
        } catch (Exception e) {
            log.error("Erro ao atualizar ordem de servico id: {}", ordemDeServico.getId(), e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public void vincularServico(Long ordemServicoId, Long servicoId, BigDecimal preco, StatusServico status) {
        try {
            ordemDeServicoServicoRepository.save(OrdemDeServicoServicoEntity.builder()
                            .ordemServicoId(ordemServicoId)
                            .servicoId(servicoId)
                            .preco(preco)
                            .status(status)
                    .build());
        } catch (Exception e) {
            log.error("Erro ao vincular servico {} na ordem de servico {}", servicoId, ordemServicoId, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    @Transactional
    public void desvincularServico(Long ordemServicoId, Long servicoId) {
        try {
            ordemDeServicoServicoRepository.deleteByOrdemServicoIdAndServicoId(ordemServicoId, servicoId);
        } catch (Exception e) {
            log.error("Erro ao desvincular servico {} da ordem de servico {}", servicoId, ordemServicoId, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public void atualizarServico(Long ordemServicoId, Long servicoId, StatusServico status, LocalDateTime dataInicioExecucao, LocalDateTime dataFimExecucao) {
        try {
            ordemDeServicoServicoRepository.atualizar(servicoId, ordemServicoId, status, dataInicioExecucao, dataFimExecucao);
        } catch (Exception e) {
            log.error("Erro ao atualizar servico {} da ordem de servico {}", servicoId, ordemServicoId, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public void vincularOuSomarPeca(Long ordemServicoId, Long pecaId, Integer quantidade, BigDecimal preco) {
        try {
            ordemDeServicoPecaRepository.findByOrdemServicoIdAndPecaId(ordemServicoId, pecaId)
                    .ifPresentOrElse(
                            e -> ordemDeServicoPecaRepository.somarQuantidade(ordemServicoId, pecaId, quantidade),
                            () -> ordemDeServicoPecaRepository.save(
                                    OrdemDeServicoPecaEntity.builder()
                                            .ordemServicoId(ordemServicoId)
                                            .pecaId(pecaId)
                                            .quantidade(quantidade)
                                            .preco(preco)
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
    public void vincularOuSomarInsumo(Long ordemServicoId, Long insumoId, Integer quantidade,  BigDecimal preco) {
        try {
            ordemDeServicoInsumoRepository.findByOrdemServicoIdAndInsumoId(ordemServicoId, insumoId)
                    .ifPresentOrElse(
                            e -> ordemDeServicoInsumoRepository.somarQuantidade(ordemServicoId, insumoId, quantidade),
                            () -> ordemDeServicoInsumoRepository.save(
                                    OrdemDeServicoInsumoEntity.builder()
                                            .ordemServicoId(ordemServicoId)
                                            .insumoId(insumoId)
                                            .quantidade(quantidade)
                                            .preco(preco)
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

    @Override
    public Pagina<OrdemDeServico> listar(int page, int size) {
        try {
            var resultado = ordemDeServicoRepository.findAll(PageRequest.of(page, size));
            var ordens = resultado.getContent().stream().map(this::mapear).toList();
            return new Pagina<>(ordens, resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(), resultado.getTotalPages());
        } catch (Exception e) {
            log.error("Erro ao listar ordens de servico", e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }
}
