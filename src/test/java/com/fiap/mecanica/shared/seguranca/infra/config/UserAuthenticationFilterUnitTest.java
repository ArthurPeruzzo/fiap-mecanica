package com.fiap.mecanica.shared.seguranca.infra.config;

import com.fiap.mecanica.shared.seguranca.core.domain.Email;
import com.fiap.mecanica.shared.seguranca.core.domain.Role;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.core.domain.User;
import com.fiap.mecanica.shared.seguranca.core.domain.password.PasswordHash;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.UserGateway;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationFilterUnitTest {

    @InjectMocks
    private UserAuthenticationFilter filter;

    @Mock
    private TokenGateway tokenGateway;

    @Mock
    private UserGateway userGateway;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Test
    void doFilterInternal_shouldSkipAuthAndPassThroughForPublicEndpoint() throws Exception {
        Mockito.when(request.getRequestURI()).thenReturn("/authenticate/login");

        filter.doFilterInternal(request, response, filterChain);

        Mockito.verify(filterChain).doFilter(request, response);
        Mockito.verifyNoInteractions(tokenGateway, userGateway);
    }

    @Test
    void doFilterInternal_shouldThrowWhenTokenIsMissingOnProtectedEndpoint() {
        Mockito.when(request.getRequestURI()).thenReturn("/api/protected");
        Mockito.when(request.getHeader("Authorization")).thenReturn(null);

        assertThrows(Exception.class, () -> filter.doFilterInternal(request, response, filterChain));

        Mockito.verifyNoInteractions(tokenGateway, userGateway);
    }

    @Test
    void doFilterInternal_shouldSetAuthenticationAndPassThroughForProtectedEndpointWithValidToken() throws Exception {
        SecurityContextHolder.clearContext();

        User user = new User(
                1L,
                new Email("user@test.com"),
                new PasswordHash("hashed"),
                List.of(new Role(1L, RoleEnum.ROLE_ATENDENTE))
        );

        Mockito.when(request.getRequestURI()).thenReturn("/api/protected");
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        Mockito.when(tokenGateway.getEmail()).thenReturn("user@test.com");
        Mockito.when(userGateway.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        filter.doFilterInternal(request, response, filterChain);

        Mockito.verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user@test.com", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilterInternal_shouldThrowWhenUserNotFoundForToken() {
        Mockito.when(request.getRequestURI()).thenReturn("/api/protected");
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        Mockito.when(tokenGateway.getEmail()).thenReturn("notfound@test.com");
        Mockito.when(userGateway.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> filter.doFilterInternal(request, response, filterChain));
    }
}
