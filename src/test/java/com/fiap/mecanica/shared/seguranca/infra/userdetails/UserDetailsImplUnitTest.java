package com.fiap.mecanica.shared.seguranca.infra.userdetails;

import com.fiap.mecanica.shared.seguranca.core.domain.Email;
import com.fiap.mecanica.shared.seguranca.core.domain.Role;
import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import com.fiap.mecanica.shared.seguranca.core.domain.User;
import com.fiap.mecanica.shared.seguranca.core.domain.password.PasswordHash;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsImplUnitTest {

    private static final User USER = new User(
            1L,
            new Email("user@test.com"),
            new PasswordHash("hashed-password"),
            List.of(
                    new Role(1L, RoleEnum.ROLE_ATENDENTE),
                    new Role(2L, RoleEnum.ROLE_MECANICO)
            )
    );

    @Test
    void getAuthorities_shouldReturnOneAuthorityPerRole() {
        UserDetailsImpl userDetails = new UserDetailsImpl(USER);

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertEquals(2, authorities.size());
    }

    @Test
    void getAuthorities_shouldReturnAuthoritiesWithCorrectRoleNames() {
        UserDetailsImpl userDetails = new UserDetailsImpl(USER);

        List<String> authorityNames = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertTrue(authorityNames.contains("ROLE_ATENDENTE"));
        assertTrue(authorityNames.contains("ROLE_MECANICO"));
    }

    @Test
    void getPassword_shouldReturnPasswordHashValue() {
        UserDetailsImpl userDetails = new UserDetailsImpl(USER);

        assertEquals("hashed-password", userDetails.getPassword());
    }

    @Test
    void getUsername_shouldReturnEmailValue() {
        UserDetailsImpl userDetails = new UserDetailsImpl(USER);

        assertEquals("user@test.com", userDetails.getUsername());
    }

    @Test
    void getUser_shouldReturnOriginalUser() {
        UserDetailsImpl userDetails = new UserDetailsImpl(USER);

        assertEquals(USER, userDetails.getUser());
    }

    @Test
    void getAuthorities_shouldReturnEmptyCollectionWhenUserHasNoRoles() {
        User userWithNoRoles = new User(1L, new Email("user@test.com"), new PasswordHash("hashed"), List.of());
        UserDetailsImpl userDetails = new UserDetailsImpl(userWithNoRoles);

        assertTrue(userDetails.getAuthorities().isEmpty());
    }
}
