package com.fiap.mecanica.ordemdeservico.infra.gateway.database;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.LinkAprovacaoOrcamento;
import com.fiap.mecanica.ordemdeservico.core.gateway.LinkAprovacaoOrcamentoGateway;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.LinkAprovacaoOrcamentoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.LinkAprovacaoOrcamentoRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class LinkAprovacaoOrcamentoDatabaseGateway implements LinkAprovacaoOrcamentoGateway {

    private final LinkAprovacaoOrcamentoRepository repository;

    @Override
    public void salvar(LinkAprovacaoOrcamento link) {
        try {
            repository.save(LinkAprovacaoOrcamentoEntity.builder()
                    .ordemServicoId(link.getOrdemDeServicoId())
                    .token(link.getToken())
                    .dataExpiracao(link.getDataExpiracao())
                    .dataUtilizacao(link.getDataUtilizacao())
                    .build());
        } catch (Exception e) {
            log.error("Erro ao criar link de aprovacao de orcamento", e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public Optional<LinkAprovacaoOrcamento> buscarPorToken(String token) {
        try {
            return repository.findByToken(token)
                    .map(entity -> LinkAprovacaoOrcamento.builder()
                            .id(entity.getId())
                            .ordemDeServicoId(entity.getOrdemServicoId())
                            .token(entity.getToken())
                            .dataExpiracao(entity.getDataExpiracao())
                            .dataUtilizacao(entity.getDataUtilizacao())
                            .build());
        } catch (Exception e) {
            log.error("Erro ao buscar link de aprovacao de orcamento por token", e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }

    @Override
    public void atualizar(LinkAprovacaoOrcamento link) {
        try {
            repository.save(LinkAprovacaoOrcamentoEntity.builder()
                    .id(link.getId())
                    .ordemServicoId(link.getOrdemDeServicoId())
                    .token(link.getToken())
                    .dataExpiracao(link.getDataExpiracao())
                    .dataUtilizacao(link.getDataUtilizacao())
                    .build());
        } catch (Exception e) {
            log.error("Erro ao atualizar link de aprovacao de orcamento", e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }
}
