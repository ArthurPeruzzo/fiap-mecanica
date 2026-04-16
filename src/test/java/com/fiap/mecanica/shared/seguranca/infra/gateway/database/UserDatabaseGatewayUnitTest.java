package com.fiap.mecanica.shared.seguranca.infra.gateway.database;

import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import com.fiap.mecanica.shared.seguranca.core.domain.Role;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.core.domain.User;
import com.fiap.mecanica.shared.seguranca.core.domain.Email;
import com.fiap.mecanica.shared.seguranca.core.domain.password.Password;
import com.fiap.mecanica.shared.seguranca.infra.gateway.entity.RoleEntity;
import com.fiap.mecanica.shared.seguranca.infra.gateway.entity.UserEntity;
import com.fiap.mecanica.shared.seguranca.infra.gateway.repository.RoleRepository;
import com.fiap.mecanica.shared.seguranca.infra.gateway.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserDatabaseGatewayUnitTest {

    @InjectMocks
    private UserDatabaseGateway gateway;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    // -------------------------------------------------------------------------
    // findByEmail
    // -------------------------------------------------------------------------

    @Test
    void findByEmail_shouldReturnMappedUserWhenFound() {
        RoleEntity roleEntity = new RoleEntity(1L, RoleEnum.ROLE_ATENDENTE);
        UserEntity userEntity = UserEntity.builder()
                .id(1L)
                .email("user@test.com")
                .password("hashed-password")
                .roles(List.of(roleEntity))
                .build();

        Mockito.when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(userEntity));

        Optional<User> result = gateway.findByEmail("user@test.com");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("user@test.com", result.get().getEmail().value());
        assertEquals("hashed-password", result.get().getPassword().getValue());
        assertEquals(1, result.get().getRoles().size());
        assertEquals(RoleEnum.ROLE_ATENDENTE, result.get().getRoles().get(0).getName());
    }

    @Test
    void findByEmail_shouldReturnEmptyWhenNotFound() {
        Mockito.when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        Optional<User> result = gateway.findByEmail("unknown@test.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByEmail_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(userRepository.findByEmail(Mockito.anyString()))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.findByEmail("user@test.com"));
    }

    // -------------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------------

    @Test
    void create_shouldReturnMappedUserAfterSave() {
        RoleEntity roleEntity = new RoleEntity(1L, RoleEnum.ROLE_ATENDENTE);
        UserEntity savedEntity = UserEntity.builder()
                .id(10L)
                .email("new@test.com")
                .password("passworD2912@")
                .roles(List.of(roleEntity))
                .build();

        User user = new User(
                new Email("new@test.com"),
                new Password("passworD2912@"),
                List.of(new Role(1L, RoleEnum.ROLE_ATENDENTE))
        );

        Mockito.when(roleRepository.findByNameIn(List.of(RoleEnum.ROLE_ATENDENTE)))
                .thenReturn(List.of(roleEntity));
        Mockito.when(userRepository.save(Mockito.any(UserEntity.class))).thenReturn(savedEntity);

        User result = gateway.create(user);

        assertEquals(10L, result.getId());
        assertEquals("new@test.com", result.getEmail().value());
        assertEquals("passworD2912@", result.getPassword().getValue());
        assertEquals(RoleEnum.ROLE_ATENDENTE, result.getRoles().getFirst().getName());
    }

    @Test
    void create_shouldThrowErroAcessoBaseDeDadosExceptionOnDataIntegrityViolation() {
        User user = new User(
                new Email("dup@test.com"),
                new Password("passworD2912@"),
                List.of(new Role(1L, RoleEnum.ROLE_ATENDENTE))
        );

        Mockito.when(roleRepository.findByNameIn(Mockito.anyList())).thenReturn(List.of());
        Mockito.when(userRepository.save(Mockito.any(UserEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate entry"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.create(user));
    }

    @Test
    void create_shouldThrowErroAcessoBaseDeDadosExceptionOnGenericException() {
        User user = new User(
                new Email("user@test.com"),
                new Password("passworD2912@"),
                List.of(new Role(1L, RoleEnum.ROLE_ATENDENTE))
        );

        Mockito.when(roleRepository.findByNameIn(Mockito.anyList())).thenReturn(List.of());
        Mockito.when(userRepository.save(Mockito.any(UserEntity.class)))
                .thenThrow(new RuntimeException("unexpected error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.create(user));
    }
}
