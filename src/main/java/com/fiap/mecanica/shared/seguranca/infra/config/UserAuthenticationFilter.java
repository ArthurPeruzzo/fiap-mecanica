package com.fiap.mecanica.shared.seguranca.infra.config;

import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import com.fiap.mecanica.shared.seguranca.infra.userdetails.UserDetailsImpl;
import com.fiap.mecanica.shared.seguranca.core.domain.User;
import com.fiap.mecanica.shared.seguranca.core.gateway.UserGateway;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.util.Arrays;

@Component
@Slf4j
@Profile({"!controller-test"})
@RequiredArgsConstructor
public class UserAuthenticationFilter extends OncePerRequestFilter {

    private final TokenGateway tokenGateway;
    private final UserGateway userGateway;
    private static final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (checkIfEndpointIsNotPublic(request)) {
            verifyHasTokenInRequest(request);
            UserDetailsImpl userDetails = getUserDetails();
            getAndSetAuthentication(userDetails);
        }

        filterChain.doFilter(request, response);
    }

    private boolean checkIfEndpointIsNotPublic(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return Arrays.stream(SecurityConfiguration.ENDPOINTS_SEM_AUTENTICACAO)
                .noneMatch(pattern -> matcher.match(pattern, requestURI));
    }

    private void verifyHasTokenInRequest(HttpServletRequest request) throws AuthenticationException {
        String token = recoveryToken(request);
        if (token == null) {
            log.warn("token ausente");
            throw new AuthenticationException("O token está ausente");
        }
    }

    private String recoveryToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null) {
            return authorizationHeader.replace("Bearer ", "");
        }
        return null;
    }

    private UserDetailsImpl getUserDetails() throws AuthenticationException {
        Long userId = tokenGateway.getUserId();
        User user = userGateway.findById(userId).orElseThrow(() -> {
            log.error("Usuário não foi encontrado para o id: {}", userId);
            return new AuthenticationException("Não foi possível processar sua solicitação");
        });
        return new UserDetailsImpl(user);
    }

    private static void getAndSetAuthentication(UserDetailsImpl userDetails) {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
