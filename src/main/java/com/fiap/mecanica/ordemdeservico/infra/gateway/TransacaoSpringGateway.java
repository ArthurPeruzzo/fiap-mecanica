package com.fiap.mecanica.ordemdeservico.infra.gateway;

import com.fiap.mecanica.ordemdeservico.core.gateway.TransacaoGateway;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class TransacaoSpringGateway implements TransacaoGateway {

	private final TransactionTemplate transactionTemplate;

	public TransacaoSpringGateway(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	@Override
	public void executar(Runnable acao) {
		transactionTemplate.executeWithoutResult(status -> acao.run());
	}
}
