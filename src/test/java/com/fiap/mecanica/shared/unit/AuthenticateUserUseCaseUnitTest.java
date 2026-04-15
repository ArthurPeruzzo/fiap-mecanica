package com.fiap.mecanica.shared.unit;

import com.fiap.mecanica.shared.seguranca.core.domain.Email;
import com.fiap.mecanica.shared.seguranca.core.domain.Role;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.core.domain.User;
import com.fiap.mecanica.shared.seguranca.core.domain.password.PasswordHash;
import com.fiap.mecanica.shared.seguranca.core.exception.BadCredentialsAuthenticateException;
import com.fiap.mecanica.shared.seguranca.core.exception.UnexpectedErrorAuthenticateException;
import com.fiap.mecanica.shared.seguranca.core.usecase.AuthenticateUserUseCase;
import com.fiap.mecanica.shared.seguranca.infra.controller.dto.LoginInputDto;
import com.fiap.mecanica.shared.seguranca.infra.token.TokenGateway;
import com.fiap.mecanica.shared.seguranca.infra.token.dto.TokenParams;
import com.fiap.mecanica.shared.seguranca.infra.userdetails.UserDetailsImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserUseCaseUnitTest {

    @InjectMocks
    private AuthenticateUserUseCase useCase;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenGateway tokenGateway;

    @Test
    void shouldThrowInternalAuthenticationServiceExceptionWhenAuthenticateFails() {
        LoginInputDto loginInputDto = new LoginInputDto("any-email", "any-password");

        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(InternalAuthenticationServiceException.class);

        var exception = Assertions.assertThrows(BadCredentialsAuthenticateException.class,
                () -> useCase.authenticate(loginInputDto));

        Mockito.verify(authenticationManager).authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class));
        Mockito.verifyNoMoreInteractions(authenticationManager);

        Mockito.verifyNoInteractions(tokenGateway);

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        Assertions.assertEquals("Usuário ou senha incorretos", exception.getMessage());
    }

    @Test
    void shouldThrowBadCredentialsAuthenticateExceptionWhenAuthenticateFails() {
        LoginInputDto loginInputDto = new LoginInputDto("any-email", "any-password");

        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        var exception = Assertions.assertThrows(BadCredentialsAuthenticateException.class,
                () -> useCase.authenticate(loginInputDto));

        Mockito.verify(authenticationManager).authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class));
        Mockito.verifyNoMoreInteractions(authenticationManager);

        Mockito.verifyNoInteractions(tokenGateway);

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        Assertions.assertEquals("Usuário ou senha incorretos", exception.getMessage());
    }

    @Test
    void shouldThrowUnexpectedErrorWhenAuthenticateFails() {
        LoginInputDto loginInputDto = new LoginInputDto("any-email", "any-password");

        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(RuntimeException.class);

        var exception = Assertions.assertThrows(UnexpectedErrorAuthenticateException.class,
                () -> useCase.authenticate(loginInputDto));

        Mockito.verify(authenticationManager).authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class));
        Mockito.verifyNoMoreInteractions(authenticationManager);

        Mockito.verifyNoInteractions(tokenGateway);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
        Assertions.assertEquals("Não foi possível realizar a autenticação", exception.getMessage());
    }

    @Test
    void shouldAuthenticateSuccessFully() {
        LoginInputDto loginInputDto = new LoginInputDto("any-email", "any-password");
        Authentication authenticationMock = Mockito.mock(Authentication.class);
        String token = "any-token";
        List<Role> roles = List.of(new Role(1L, RoleEnum.ROLE_ATENDENTE));

        User user = new User(1L, new Email("default@email.com"), new PasswordHash("any-hash"), roles);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticationMock);

        Mockito.when(authenticationMock.getPrincipal()).thenReturn(userDetails);

        Mockito.when(tokenGateway.generateToken(Mockito.any(TokenParams.class))).thenReturn(token);

        String tokenResult = Assertions.assertDoesNotThrow(() -> useCase.authenticate(loginInputDto));

        Assertions.assertEquals(token, tokenResult);

        Mockito.verify(authenticationManager).authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class));
        Mockito.verifyNoMoreInteractions(authenticationManager);

        Mockito.verify(authenticationMock).getPrincipal();
        Mockito.verify(tokenGateway).generateToken(Mockito.any(TokenParams.class));
    }


}
