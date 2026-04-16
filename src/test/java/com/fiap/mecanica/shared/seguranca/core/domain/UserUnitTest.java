package com.fiap.mecanica.shared.seguranca.core.domain;

import com.fiap.mecanica.shared.seguranca.core.domain.password.Password;
import com.fiap.mecanica.shared.seguranca.core.domain.password.PasswordHash;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserUnitTest {

    private static final Email EMAIL = new Email("user@example.com");
    private static final PasswordHash PASSWORD_HASH = new PasswordHash("hashed-password");
    private static final List<Role> ROLES = List.of(
            new Role(1L, RoleEnum.ROLE_ATENDENTE),
            new Role(2L, RoleEnum.ROLE_MECANICO)
    );

    @Test
    void shouldCreateUserWithAllArgsConstructor() {
        User user = new User(1L, EMAIL, PASSWORD_HASH, ROLES);

        assertEquals(1L, user.getId());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(PASSWORD_HASH, user.getPassword());
        assertEquals(ROLES, user.getRoles());
    }

    @Test
    void shouldCreateUserWithEmailPasswordAndRoles() {
        Password password = new Password("Senha@123");
        User user = new User(EMAIL, password, ROLES);

        assertNull(user.getId());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(ROLES, user.getRoles());
    }

    @Test
    void shouldCreateUserWithNoArgsConstructor() {
        User user = new User();

        assertNull(user.getId());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getRoles());
    }

    @Test
    void shouldUpdatePasswordViaSetPassword() {
        User user = new User(1L, EMAIL, PASSWORD_HASH, ROLES);
        PasswordHash newHash = new PasswordHash("new-hashed-password");

        user.setPassword(newHash);

        assertEquals(newHash, user.getPassword());
    }

    @Test
    void shouldReturnRolesFormattedAsStringList() {
        User user = new User(1L, EMAIL, PASSWORD_HASH, ROLES);

        List<String> formatted = user.getRolesFormattedAsString();

        assertEquals(List.of("ROLE_ATENDENTE", "ROLE_MECANICO"), formatted);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoRoles() {
        User user = new User(1L, EMAIL, PASSWORD_HASH, List.of());

        List<String> formatted = user.getRolesFormattedAsString();

        assertTrue(formatted.isEmpty());
    }

    @Test
    void shouldExcludePasswordFromToString() {
        User user = new User(1L, EMAIL, PASSWORD_HASH, ROLES);

        String result = user.toString();

        assertFalse(result.contains("password"));
        assertFalse(result.contains("hashed-password"));
    }
}
