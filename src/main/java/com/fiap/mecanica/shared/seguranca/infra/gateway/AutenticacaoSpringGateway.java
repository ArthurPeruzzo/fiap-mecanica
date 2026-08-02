package com.fiap.mecanica.shared.seguranca.infra.gateway;

import com.fiap.mecanica.shared.seguranca.core.domain.User;
import com.fiap.mecanica.shared.seguranca.core.exception.BadCredentialsAuthenticateException;
import com.fiap.mecanica.shared.seguranca.core.exception.UnexpectedErrorAuthenticateException;
import com.fiap.mecanica.shared.seguranca.core.gateway.AutenticacaoGateway;
import com.fiap.mecanica.shared.seguranca.infra.userdetails.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AutenticacaoSpringGateway implements AutenticacaoGateway {

    private final AuthenticationManager authenticationManager;

    @Override
    public User autenticar(String cpf, String senha) {
        try {
            var token = new UsernamePasswordAuthenticationToken(cpf, senha);
            var auth = authenticationManager.authenticate(token);
            if (!(auth.getPrincipal() instanceof UserDetailsImpl userDetails)) {
                log.error("Principal retornado nao e do tipo esperado: {}", auth.getPrincipal());
                throw new UnexpectedErrorAuthenticateException();
            }
            return userDetails.getUser();
        } catch (UnexpectedErrorAuthenticateException e) {
            throw e;
        } catch (InternalAuthenticationServiceException | BadCredentialsException e) {
            log.error("Usuario ou senha informados estao incorretos", e);
            throw new BadCredentialsAuthenticateException();
        } catch (Exception e) {
            log.error("Erro inesperado no processo de autenticacao", e);
            throw new UnexpectedErrorAuthenticateException();
        }
    }
}
