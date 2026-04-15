package com.fiap.mecanica.shared.seguranca.infra.gateway.entity;

import com.fiap.mecanica.shared.seguranca.core.domain.RoleEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="roles")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoleEnum name;


    public RoleEntity(RoleEnum name) {
        this.name = name;
    }
}
