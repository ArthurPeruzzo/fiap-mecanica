package com.fiap.mecanica.ordemdeservico.core.usecase.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.LinkAprovacaoOrcamento;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.mensagem.MensagemOrcamentoEnviadoFactory;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoNaoEncontradaException;
import com.fiap.mecanica.ordemdeservico.core.gateway.LinkAprovacaoOrcamentoGateway;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.shared.notificacao.core.domain.MensagemParams;
import com.fiap.mecanica.shared.notificacao.core.gateway.NotificacaoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnviarOrcamentoOrdemDeServicoUseCase {

    @Value("${url.recusar.orcamento}")
    private String urlRecusarOrcamento;

    @Value("${url.aprovar.orcamento}")
    private String urlAprovarOrcamento;

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final LinkAprovacaoOrcamentoGateway linkAprovacaoOrcamentoGateway;
    private final NotificacaoGateway notificacaoGateway;

    public void enviar(Long ordemServicoId) {
        OrdemDeServico ordemDeServico = ordemDeServicoGateway.buscarPorId(ordemServicoId)
                .orElseThrow(OrdemDeServicoNaoEncontradaException::new);

        ordemDeServico.gravarEnvioOrcamento();
        ordemDeServicoGateway.atualizar(ordemDeServico);

        LinkAprovacaoOrcamento link = new LinkAprovacaoOrcamento(ordemServicoId);
        linkAprovacaoOrcamentoGateway.salvar(link);

        var params = MensagemParams.builder()
                .clienteId(ordemDeServico.getClienteId())
                .ordemId(ordemServicoId)
                .valorTotal(ordemDeServico.getOrcamento().valorTotal())
                .token(link.getToken())
                .urlAprovar(urlAprovarOrcamento)
                .urlRecusar(urlRecusarOrcamento)
                .build();
        notificacaoGateway.enviar(new MensagemOrcamentoEnviadoFactory().criar(params));
    }
}
