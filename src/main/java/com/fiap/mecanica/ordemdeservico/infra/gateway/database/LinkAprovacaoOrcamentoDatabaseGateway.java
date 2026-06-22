package com.fiap.mecanica.ordemdeservico.infra.gateway.database;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.LinkAprovacaoOrcamento;
import com.fiap.mecanica.ordemdeservico.core.gateway.LinkAprovacaoOrcamentoGateway;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.LinkAprovacaoOrcamentoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.LinkAprovacaoOrcamentoRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
}
