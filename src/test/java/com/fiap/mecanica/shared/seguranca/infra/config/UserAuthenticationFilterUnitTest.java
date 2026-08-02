package com.fiap.mecanica.shared.seguranca.infra.config;

import com.fiap.mecanica.shared.seguranca.core.domain.Role;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.core.domain.User;
import com.fiap.mecanica.shared.seguranca.core.domain.password.PasswordHash;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.UserGateway;
import com.fiap.mecanica.shared.valueobjects.Cpf;
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
                new Cpf("52998224725"),
                new PasswordHash("hashed"),
                List.of(new Role(1L, RoleEnum.ROLE_ATENDENTE))
        );

        Mockito.when(request.getRequestURI()).thenReturn("/api/protected");
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        Mockito.when(tokenGateway.getUserId()).thenReturn(1L);
        Mockito.when(userGateway.findById(1L)).thenReturn(Optional.of(user));

        filter.doFilterInternal(request, response, filterChain);

        Mockito.verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("52998224725", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilterInternal_shouldThrowWhenUserNotFoundForToken() {
        Mockito.when(request.getRequestURI()).thenReturn("/api/protected");
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        Mockito.when(tokenGateway.getUserId()).thenReturn(999L);
        Mockito.when(userGateway.findById(999L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> filter.doFilterInternal(request, response, filterChain));
    }
}
