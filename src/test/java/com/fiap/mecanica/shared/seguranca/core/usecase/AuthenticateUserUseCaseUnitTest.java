package com.fiap.mecanica.shared.seguranca.core.usecase;

import com.fiap.mecanica.shared.seguranca.core.domain.Role;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.core.domain.User;
import com.fiap.mecanica.shared.seguranca.core.domain.password.PasswordHash;
import com.fiap.mecanica.shared.seguranca.core.exception.BadCredentialsAuthenticateException;
import com.fiap.mecanica.shared.seguranca.core.exception.UnexpectedErrorAuthenticateException;
import com.fiap.mecanica.shared.seguranca.core.gateway.AutenticacaoGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import com.fiap.mecanica.shared.valueobjects.Cpf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserUseCaseUnitTest {

    @Mock
    private AutenticacaoGateway autenticacaoGateway;

    @Mock
    private TokenGateway tokenGateway;

    @Mock
    private AutenticarOutputPort outputPort;

    private AuthenticateUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AuthenticateUserUseCase(autenticacaoGateway, tokenGateway, outputPort);
    }

    @Test
    void shouldPropagateExceptionWhenGatewayThrowsBadCredentials() {
        Mockito.when(autenticacaoGateway.autenticar(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new BadCredentialsAuthenticateException());

        var exception = Assertions.assertThrows(BadCredentialsAuthenticateException.class,
                () -> useCase.authenticate("52998224725", "wrong-password"));

        Mockito.verify(autenticacaoGateway).autenticar("52998224725", "wrong-password");
        Mockito.verifyNoInteractions(tokenGateway, outputPort);

        Assertions.assertEquals(401, exception.getStatusCode());
        Assertions.assertEquals("Usuário ou senha incorretos", exception.getMessage());
    }

    @Test
    void shouldPropagateExceptionWhenGatewayThrowsUnexpectedError() {
        Mockito.when(autenticacaoGateway.autenticar(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new UnexpectedErrorAuthenticateException());

        var exception = Assertions.assertThrows(UnexpectedErrorAuthenticateException.class,
                () -> useCase.authenticate("52998224725", "any-password"));

        Mockito.verify(autenticacaoGateway).autenticar("52998224725", "any-password");
        Mockito.verifyNoInteractions(tokenGateway, outputPort);

        Assertions.assertEquals(500, exception.getStatusCode());
        Assertions.assertEquals("Não foi possível realizar a autenticação", exception.getMessage());
    }

    @Test
    void shouldCallOutputPortWithTokenWhenAuthenticationSucceeds() {
        var user = new User(1L, new Cpf("52998224725"), new PasswordHash("hash"),
                List.of(new Role(1L, RoleEnum.ROLE_ATENDENTE)));
        String expectedToken = "Bearer any-token";

        Mockito.when(autenticacaoGateway.autenticar("52998224725", "correct-password"))
                .thenReturn(user);
        Mockito.when(tokenGateway.generateToken(user))
                .thenReturn(expectedToken);

        useCase.authenticate("52998224725", "correct-password");

        Mockito.verify(autenticacaoGateway).autenticar("52998224725", "correct-password");
        Mockito.verify(tokenGateway).generateToken(user);
        Mockito.verify(outputPort).apresentar(expectedToken);
    }

    @Test
    void shouldStripFormattingFromCpfBeforeCallingGateway() {
        var user = new User(1L, new Cpf("52998224725"), new PasswordHash("hash"),
                List.of(new Role(1L, RoleEnum.ROLE_ATENDENTE)));

        Mockito.when(autenticacaoGateway.autenticar("52998224725", "correct-password"))
                .thenReturn(user);
        Mockito.when(tokenGateway.generateToken(user))
                .thenReturn("Bearer any-token");

        useCase.authenticate("529.982.247-25", "correct-password");

        Mockito.verify(autenticacaoGateway).autenticar("52998224725", "correct-password");
    }
}
