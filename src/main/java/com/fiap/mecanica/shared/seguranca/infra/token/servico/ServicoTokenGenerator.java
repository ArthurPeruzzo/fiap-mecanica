package com.fiap.mecanica.shared.seguranca.infra.token.servico;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.UUID;

/**
 * Gera o token com que este serviço se identifica ao chamar outro serviço (hoje, o fiap-estoque). É um JWT
 * curto, assinado com um segredo próprio (diferente do JWT de usuário), destinado a uma audiência e com as
 * permissões na claim "scope" (separadas por espaço). Gerado a cada chamada: assinar é barato e evita guardar
 * estado compartilhado.
 */
@Component
public class ServicoTokenGenerator {

	private final Algorithm algorithm;
	private final String issuer;
	private final String subject;
	private final Duration ttl;

	public ServicoTokenGenerator(@Value("${servico.jwt.secret}") String secret,
								 @Value("${servico.jwt.issuer}") String issuer,
								 @Value("${servico.jwt.subject}") String subject,
								 @Value("${servico.jwt.ttl}") Duration ttl) {
		this.algorithm = Algorithm.HMAC256(Base64.getUrlDecoder().decode(secret));
		this.issuer = issuer;
		this.subject = subject;
		this.ttl = ttl;
	}

	public String gerar(String audiencia, Collection<String> escopos) {
		Instant agora = Instant.now();
		return JWT.create()
				.withIssuer(issuer)
				.withSubject(subject)
				.withAudience(audiencia)
				.withClaim("scope", String.join(" ", escopos))
				.withIssuedAt(agora)
				.withExpiresAt(agora.plus(ttl))
				.withJWTId(UUID.randomUUID().toString())
				.sign(algorithm);
	}
}
