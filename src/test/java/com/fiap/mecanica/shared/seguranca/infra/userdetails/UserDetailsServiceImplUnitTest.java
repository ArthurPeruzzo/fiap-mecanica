package com.fiap.mecanica.shared.seguranca.infra.userdetails;

import com.fiap.mecanica.shared.seguranca.core.domain.Role;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.core.domain.User;
import com.fiap.mecanica.shared.seguranca.core.domain.password.PasswordHash;
import com.fiap.mecanica.shared.seguranca.core.gateway.UserGateway;
import com.fiap.mecanica.shared.valueobjects.Cpf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
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
                new Cpf("52998224725"),
                new PasswordHash("hashed-password"),
                List.of(new Role(1L, RoleEnum.ROLE_ATENDENTE))
        );

        Mockito.when(userGateway.findByCpf("52998224725")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("52998224725");

        assertNotNull(result);
        assertInstanceOf(UserDetailsImpl.class, result);
        assertEquals("52998224725", result.getUsername());
        assertEquals("hashed-password", result.getPassword());
    }

    @Test
    void loadUserByUsername_shouldThrowNoSuchElementExceptionWhenUserNotFound() {
        Mockito.when(userGateway.findByCpf("00000000000")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("00000000000"));
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetailsWithCorrectAuthorities() {
        User user = new User(
                1L,
                new Cpf("52998224725"),
                new PasswordHash("hashed"),
                List.of(
                        new Role(1L, RoleEnum.ROLE_ATENDENTE),
                        new Role(2L, RoleEnum.ROLE_MECANICO)
                )
        );

        Mockito.when(userGateway.findByCpf("52998224725")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("52998224725");

        assertEquals(2, result.getAuthorities().size());
    }
}
