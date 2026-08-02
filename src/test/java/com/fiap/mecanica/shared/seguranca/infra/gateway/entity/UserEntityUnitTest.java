package com.fiap.mecanica.shared.seguranca.infra.gateway.entity;

import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityUnitTest {

    @Test
    void constructor_withCpfPasswordAndRoles_shouldSetFieldsCorrectly() {
        RoleEntity role = new RoleEntity(RoleEnum.ROLE_ATENDENTE);
        UserEntity entity = new UserEntity("52998224725", "hashed-password", List.of(role));

        assertEquals("52998224725", entity.getCpf());
        assertEquals("hashed-password", entity.getPassword());
        assertEquals(1, entity.getRoles().size());
        assertEquals(RoleEnum.ROLE_ATENDENTE, entity.getRoles().get(0).getName());
    }

    @Test
    void constructor_withUserId_shouldSetIdOnly() {
        UserEntity entity = new UserEntity(42L);

        assertEquals(42L, entity.getId());
        assertNull(entity.getCpf());
        assertNull(entity.getPassword());
    }

    @Test
    void builder_shouldCreateEntityWithAllFields() {
        RoleEntity role = new RoleEntity(1L, RoleEnum.ROLE_MECANICO);
        UserEntity entity = UserEntity.builder()
                .id(1L)
                .cpf("11144477735")
                .password("hashed")
                .roles(List.of(role))
                .build();

        assertEquals(1L, entity.getId());
        assertEquals("11144477735", entity.getCpf());
        assertEquals("hashed", entity.getPassword());
        assertEquals(RoleEnum.ROLE_MECANICO, entity.getRoles().get(0).getName());
    }

    @Test
    void setRoles_shouldUpdateRoles() {
        UserEntity entity = new UserEntity("52998224725", "hashed", List.of());
        RoleEntity newRole = new RoleEntity(RoleEnum.ROLE_MECANICO);

        entity.setRoles(List.of(newRole));

        assertEquals(1, entity.getRoles().size());
        assertEquals(RoleEnum.ROLE_MECANICO, entity.getRoles().get(0).getName());
    }
}
