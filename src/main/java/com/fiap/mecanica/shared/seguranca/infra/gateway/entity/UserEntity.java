package com.fiap.mecanica.shared.seguranca.infra.gateway.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name="users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cpf", nullable = false, unique = true)
    private String cpf;

    @Column(name = "password", nullable = false)
    private String password;

    @Setter
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name="role_id"))
    private List<RoleEntity> roles;

    public UserEntity(String cpf, String password, List<RoleEntity> roles) {
        this.cpf = cpf;
        this.password = password;
        this.roles = roles;
    }

    public UserEntity(Long userId) {
        this.id = userId;
    }
}
