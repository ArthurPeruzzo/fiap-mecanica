package com.fiap.mecanica.shared.seguranca.infra.gateway;

import com.fiap.mecanica.shared.seguranca.core.domain.Email;
import com.fiap.mecanica.shared.seguranca.core.domain.Role;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.core.domain.User;
import com.fiap.mecanica.shared.seguranca.core.domain.password.PasswordHash;
import com.fiap.mecanica.shared.seguranca.core.exception.BadCredentialsAuthenticateException;
import com.fiap.mecanica.shared.seguranca.core.exception.UnexpectedErrorAuthenticateException;
import com.fiap.mecanica.shared.seguranca.infra.userdetails.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AutenticacaoSpringGatewayUnitTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    private AutenticacaoSpringGateway gateway;

    private static final User USER = new User(
            1L,
            new Email("user@test.com"),
            new PasswordHash("hash"),
            List.of(new Role(1L, RoleEnum.ROLE_ATENDENTE))
    );

    @BeforeEach
    void setUp() {
        gateway = new AutenticacaoSpringGateway(authenticationManager);
    }

    @Test
    void shouldReturnUserWhenCredentialsAreValid() {
        var userDetails = new UserDetailsImpl(USER);

        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);

        User result = gateway.autenticar("user@test.com", "correct-password");

        assertEquals(USER, result);
        Mockito.verify(authenticationManager).authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldThrowBadCredentialsWhenAuthenticationManagerThrowsBadCredentialsException() {
        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        var exception = assertThrows(BadCredentialsAuthenticateException.class,
                () -> gateway.autenticar("user@test.com", "wrong-password"));

        assertEquals(401, exception.getStatusCode());
        assertEquals("Usuário ou senha incorretos", exception.getMessage());
    }

    @Test
    void shouldThrowBadCredentialsWhenAuthenticationManagerThrowsInternalAuthenticationServiceException() {
        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new InternalAuthenticationServiceException("internal error"));

        var exception = assertThrows(BadCredentialsAuthenticateException.class,
                () -> gateway.autenticar("user@test.com", "any-password"));

        assertEquals(401, exception.getStatusCode());
        assertEquals("Usuário ou senha incorretos", exception.getMessage());
    }

    @Test
    void shouldThrowUnexpectedErrorWhenAuthenticationManagerThrowsGenericException() {
        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("unexpected"));

        var exception = assertThrows(UnexpectedErrorAuthenticateException.class,
                () -> gateway.autenticar("user@test.com", "any-password"));

        assertEquals(500, exception.getStatusCode());
        assertEquals("Não foi possível realizar a autenticação", exception.getMessage());
    }

    @Test
    void shouldThrowUnexpectedErrorWhenPrincipalIsNotUserDetailsImpl() {
        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn("unexpected-principal");

        var exception = assertThrows(UnexpectedErrorAuthenticateException.class,
                () -> gateway.autenticar("user@test.com", "any-password"));

        assertEquals(500, exception.getStatusCode());
        assertEquals("Não foi possível realizar a autenticação", exception.getMessage());
    }

    @Test
    void shouldThrowUnexpectedErrorWhenPrincipalIsNull() {
        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(null);

        assertThrows(UnexpectedErrorAuthenticateException.class,
                () -> gateway.autenticar("user@test.com", "any-password"));
    }

    @Test
    void shouldPassCorrectCredentialsToAuthenticationManager() {
        var userDetails = new UserDetailsImpl(USER);

        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);

        gateway.autenticar("user@test.com", "my-password");

        var captor = org.mockito.ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        Mockito.verify(authenticationManager).authenticate(captor.capture());
        assertEquals("user@test.com", captor.getValue().getPrincipal());
        assertEquals("my-password", captor.getValue().getCredentials());
    }
}
