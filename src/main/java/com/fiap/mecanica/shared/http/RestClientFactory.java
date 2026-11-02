package com.fiap.mecanica.shared.http;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

/**
 * Monta os {@link RestClient} usados para chamar outros serviços, com a configuração comum (HTTP/1.1
 * e timeouts) e os interceptors opcionais (ex.: autenticação). Cada integração cria o seu cliente
 * informando a URL base e os timeouts.
 * <p>
 * Os clientes são construídos com o {@link ObservationRegistry} do Spring: é ele que gera o span de cada chamada HTTP
 * e injeta o cabeçalho {@code traceparent}, para o trace da requisição continuar no serviço chamado. O
 * {@code RestClient.builder()} puro não é instrumentado.
 */
@Component
public class RestClientFactory {

	private final ObservationRegistry observationRegistry;

	@Autowired
	public RestClientFactory(ObservationRegistry observationRegistry) {
		this.observationRegistry = observationRegistry;
	}

	/** Sem observabilidade (testes unitários que montam o cliente fora do Spring). */
	public RestClientFactory() {
		this(ObservationRegistry.NOOP);
	}

	public RestClient criar(String baseUrl, Duration connectTimeout, Duration readTimeout,
							ClientHttpRequestInterceptor... interceptors) {
		var httpClient = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_1_1)
				.connectTimeout(connectTimeout)
				.build();
		var requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(readTimeout);

		return RestClient.builder()
				.observationRegistry(observationRegistry)
				.baseUrl(baseUrl)
				.requestFactory(requestFactory)
				.requestInterceptors(lista -> lista.addAll(List.of(interceptors)))
				.build();
	}
}
