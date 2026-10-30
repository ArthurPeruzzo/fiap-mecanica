package com.fiap.mecanica.ordemdeservico.infra.gateway.estoque;

import com.fiap.mecanica.shared.seguranca.infra.token.servico.ServicoTokenGenerator;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.List;

/**
 * Autentica cada chamada ao fiap-estoque com um token de serviço novo, com o menor escopo que a rota exige
 * (menor privilégio): /catalogo/** só lê o catálogo e /reserva/** só escreve reservas. Rota desconhecida segue
 * sem token e o estoque a recusa com 401.
 */
public class EstoqueTokenInterceptor implements ClientHttpRequestInterceptor {

	static final String AUDIENCIA = "fiap-estoque";
	static final String ESCOPO_CATALOGO = "catalogo:leitura";
	static final String ESCOPO_RESERVA = "reserva:escrita";

	private final ServicoTokenGenerator tokenGenerator;

	public EstoqueTokenInterceptor(ServicoTokenGenerator tokenGenerator) {
		this.tokenGenerator = tokenGenerator;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		String caminho = request.getURI().getRawPath();
		if (caminho.startsWith("/catalogo/")) {
			request.getHeaders().setBearerAuth(tokenGenerator.gerar(AUDIENCIA, List.of(ESCOPO_CATALOGO)));
		} else if (caminho.startsWith("/reserva/")) {
			request.getHeaders().setBearerAuth(tokenGenerator.gerar(AUDIENCIA, List.of(ESCOPO_RESERVA)));
		}
		return execution.execute(request, body);
	}
}
