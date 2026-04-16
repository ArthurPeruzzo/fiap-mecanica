package com.fiap.mecanica.shared.seguranca.infra.gateway.entity;

import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleEntityUnitTest {

    @Test
    void constructor_withRoleEnum_shouldSetName() {
        RoleEntity entity = new RoleEntity(RoleEnum.ROLE_ATENDENTE);

        assertEquals(RoleEnum.ROLE_ATENDENTE, entity.getName());
    }

    @Test
    void constructor_withIdAndRoleEnum_shouldSetBothFields() {
        RoleEntity entity = new RoleEntity(1L, RoleEnum.ROLE_MECANICO);

        assertEquals(1L, entity.getId());
        assertEquals(RoleEnum.ROLE_MECANICO, entity.getName());
    }

    @Test
    void builder_shouldCreateEntityWithAllFields() {
        RoleEntity entity = RoleEntity.builder()
                .id(5L)
                .name(RoleEnum.ROLE_ATENDENTE)
                .build();

        assertEquals(5L, entity.getId());
        assertEquals(RoleEnum.ROLE_ATENDENTE, entity.getName());
    }

    @Test
    void noArgsConstructor_shouldCreateEntityWithNullFields() {
        RoleEntity entity = new RoleEntity();

        assertNull(entity.getId());
        assertNull(entity.getName());
    }
}
