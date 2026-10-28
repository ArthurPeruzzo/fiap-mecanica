package com.fiap.mecanica.ordemdeservico.infra.gateway.estoque;

import com.fiap.mecanica.shared.http.RestClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class EstoqueClientConfig {

	@Bean
	public RestClient estoqueRestClient(RestClientFactory restClientFactory,
										@Value("${estoque.url}") String baseUrl,
										@Value("${estoque.connect-timeout}") Duration connectTimeout,
										@Value("${estoque.read-timeout}") Duration readTimeout) {
		return restClientFactory.criar(baseUrl, connectTimeout, readTimeout);
	}
}
