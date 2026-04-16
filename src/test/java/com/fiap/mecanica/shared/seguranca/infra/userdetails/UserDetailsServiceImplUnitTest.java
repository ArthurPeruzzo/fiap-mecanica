package com.fiap.mecanica.shared.seguranca.infra.userdetails;

import com.fiap.mecanica.shared.seguranca.core.domain.Email;
import com.fiap.mecanica.shared.seguranca.core.domain.Role;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.core.domain.User;
import com.fiap.mecanica.shared.seguranca.core.domain.password.PasswordHash;
import com.fiap.mecanica.shared.seguranca.core.gateway.UserGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplUnitTest {

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private UserGateway userGateway;

    @Test
    void loadUserByUsername_shouldReturnUserDetailsWhenUserFound() {
        User user = new User(
                1L,
                new Email("user@test.com"),
                new PasswordHash("hashed-password"),
                List.of(new Role(1L, RoleEnum.ROLE_ATENDENTE))
        );

        Mockito.when(userGateway.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("user@test.com");

        assertNotNull(result);
        assertInstanceOf(UserDetailsImpl.class, result);
        assertEquals("user@test.com", result.getUsername());
        assertEquals("hashed-password", result.getPassword());
    }

    @Test
    void loadUserByUsername_shouldThrowNoSuchElementExceptionWhenUserNotFound() {
        Mockito.when(userGateway.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> userDetailsService.loadUserByUsername("unknown@test.com"));
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetailsWithCorrectAuthorities() {
        User user = new User(
                1L,
                new Email("user@test.com"),
                new PasswordHash("hashed"),
                List.of(
                        new Role(1L, RoleEnum.ROLE_ATENDENTE),
                        new Role(2L, RoleEnum.ROLE_MECANICO)
                )
        );

        Mockito.when(userGateway.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("user@test.com");

        assertEquals(2, result.getAuthorities().size());
    }
}
