package com.fiap.mecanica.ordemdeservico.infra.gateway.database;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoEntity;
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

    @Override
    public void criar(OrdemDeServico ordemDeServico) {
        try {
            ordemDeServicoRepository.save(OrdemDeServicoEntity.builder()
                    .clienteId(ordemDeServico.getClienteId())
                    .veiculoId(ordemDeServico.getVeiculoId())
                    .atendenteId(ordemDeServico.getAtendenteId())
                    .status(ordemDeServico.getStatus())
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
            return ordemDeServicoRepository.findById(id)
                    .map(entity -> OrdemDeServico.reconstituir(
                            entity.getId(),
                            entity.getClienteId(),
                            entity.getVeiculoId(),
                            entity.getAtendenteId(),
                            entity.getMecanicoId(),
                            entity.getStatus(),
                            entity.getDataCriacao(),
                            entity.getDataInicioDiagnostico(),
                            entity.getDataConclusaoDiagnostico()
                    ));
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
            ordemDeServicoRepository.save(OrdemDeServicoEntity.builder()
                    .id(ordemDeServico.getId())
                    .clienteId(ordemDeServico.getClienteId())
                    .veiculoId(ordemDeServico.getVeiculoId())
                    .atendenteId(ordemDeServico.getAtendenteId())
                    .mecanicoId(ordemDeServico.getMecanicoId())
                    .status(ordemDeServico.getStatus())
                    .dataCriacao(ordemDeServico.getDataCriacao())
                    .dataInicioDiagnostico(ordemDeServico.getDataInicioDiagnostico())
                    .dataConclusaoDiagnostico(ordemDeServico.getDataConclusaoDiagnostico())
                    .build());
        } catch (Exception e) {
            log.error("Erro ao atualizar ordem de servico id: {}", ordemDeServico.getId(), e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }
}
