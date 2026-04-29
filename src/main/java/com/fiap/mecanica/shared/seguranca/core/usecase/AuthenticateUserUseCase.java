package com.fiap.mecanica.shared.seguranca.core.usecase;

import com.fiap.mecanica.shared.seguranca.core.domain.User;
import com.fiap.mecanica.shared.seguranca.core.exception.BadCredentialsAuthenticateException;
import com.fiap.mecanica.shared.seguranca.core.exception.UnexpectedErrorAuthenticateException;
import com.fiap.mecanica.shared.seguranca.infra.controller.dto.LoginInputDto;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import com.fiap.mecanica.shared.seguranca.infra.token.dto.TokenParams;
import com.fiap.mecanica.shared.seguranca.infra.userdetails.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticateUserUseCase {

	private final AuthenticationManager authenticationManager;
	private final TokenGateway tokenGateway;

	public String authenticate(LoginInputDto loginInputDto) {
		Authentication authenticate = authentication(loginInputDto);

		Object principal = authenticate.getPrincipal();
		if (!(principal instanceof UserDetailsImpl userDetails)) {
			log.error("Principal retornado nao e do tipo esperado: {}", principal);
			throw new UnexpectedErrorAuthenticateException();
		}
		User user = userDetails.getUser();

		TokenParams tokenParams = new TokenParams(user.getId(), user.getEmail().value(), user.getRolesFormattedAsString());

		return tokenGateway.generateToken(tokenParams);
	}

	private Authentication authentication(LoginInputDto loginInputDto) {
		try {
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
					new UsernamePasswordAuthenticationToken(loginInputDto.email(), loginInputDto.password());

			return authenticationManager.authenticate(usernamePasswordAuthenticationToken);
		} catch (InternalAuthenticationServiceException e) {
			log.error("Erro interno no processo de autenticacao", e);
			throw new BadCredentialsAuthenticateException();
		} catch (BadCredentialsException e) {
			log.error("Usuario ou senha informados estao incorretos", e);
			throw new BadCredentialsAuthenticateException();
		} catch (Exception e) {
			log.error("Erro inesperado no processo de autenticacao", e);
			throw new UnexpectedErrorAuthenticateException();
		}
	}

}
