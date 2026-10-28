package com.fiap.mecanica.shared.http;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Monta os {@link RestClient} usados para chamar outros serviços, com a configuração comum (HTTP/1.1
 * e timeouts). Cada integração cria o seu cliente informando a URL base e os timeouts.
 */
@Component
public class RestClientFactory {

	public RestClient criar(String baseUrl, Duration connectTimeout, Duration readTimeout) {
		var httpClient = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_1_1)
				.connectTimeout(connectTimeout)
				.build();
		var requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(readTimeout);

		return RestClient.builder()
				.baseUrl(baseUrl)
				.requestFactory(requestFactory)
				.build();
	}
}
