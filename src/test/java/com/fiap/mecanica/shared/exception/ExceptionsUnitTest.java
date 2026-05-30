package com.fiap.mecanica.shared.exception;

import com.fiap.mecanica.shared.seguranca.core.exception.BadCredentialsAuthenticateException;
import com.fiap.mecanica.shared.seguranca.core.exception.UnexpectedErrorAuthenticateException;
import com.fiap.mecanica.shared.seguranca.core.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ExceptionsUnitTest {

    // -------------------------------------------------------------------------
    // BaseException
    // -------------------------------------------------------------------------

    @Test
    void baseException_shouldStoreStatusAndMessage() {
        BaseException exception = new BaseException(400, "mensagem de teste") {};

        assertEquals(400, exception.getStatusCode());
        assertEquals("mensagem de teste", exception.getMessage());
    }

    @Test
    void baseException_shouldExtendRuntimeException() {
        BaseException exception = new BaseException(400, "mensagem de teste") {};

        assertInstanceOf(RuntimeException.class, exception);
    }

    // -------------------------------------------------------------------------
    // ErroAcessoBaseDeDadosException
    // -------------------------------------------------------------------------

    @Test
    void erroAcessoBaseDeDadosException_shouldExtendedBaseExceptionAndReturnInternalServerErrorAndCorrectMessage() {
        ErroAcessoBaseDeDadosException exception = new ErroAcessoBaseDeDadosException();

        assertInstanceOf(BaseException.class, exception);
        assertEquals(500, exception.getStatusCode());
        assertEquals("Erro ao acessar base de dados", exception.getMessage());
    }

    // -------------------------------------------------------------------------
    // UserNotFoundException
    // -------------------------------------------------------------------------

    @Test
    void userNotFound_shouldExtendBaseExceptionAndReturnNotFoundAndCorrectMessage() {
        UserNotFoundException exception = new UserNotFoundException();

        assertInstanceOf(BaseException.class, exception);
        assertEquals(404, exception.getStatusCode());
        assertEquals("Usuario nao encontrado", exception.getMessage());
    }

    // -------------------------------------------------------------------------
    // BadCredentialsAuthenticateException
    // -------------------------------------------------------------------------

    @Test
    void badCredentials_shouldExtendBaseExceptionAndReturnUnauthorizedAndCorrectMessage() {
        BadCredentialsAuthenticateException exception = new BadCredentialsAuthenticateException();

        assertInstanceOf(BaseException.class, exception);
        assertEquals(401, exception.getStatusCode());
        assertEquals("Usuário ou senha incorretos", exception.getMessage());
    }

    // -------------------------------------------------------------------------
    // UnexpectedErrorAuthenticateException
    // -------------------------------------------------------------------------

    @Test
    void unexpectedError_shouldExtendBaseExceptionAndReturnInternalServerErrorAndCorrectMessage() {
        UnexpectedErrorAuthenticateException exception = new UnexpectedErrorAuthenticateException();

        assertInstanceOf(BaseException.class, exception);
        assertEquals(500, exception.getStatusCode());
        assertEquals("Não foi possível realizar a autenticação", exception.getMessage());
    }
}
