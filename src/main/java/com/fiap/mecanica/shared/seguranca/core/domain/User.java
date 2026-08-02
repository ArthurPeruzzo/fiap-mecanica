package com.fiap.mecanica.shared.seguranca.core.domain;

import com.fiap.mecanica.shared.seguranca.core.domain.password.Password;
import com.fiap.mecanica.shared.seguranca.core.domain.password.PasswordBase;
import com.fiap.mecanica.shared.valueobjects.Cpf;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(exclude = "password")
public class User implements Serializable {
    private Long id;
    private Cpf cpf;

    @Setter
    private PasswordBase password;

    private List<Role> roles;

    public User(Cpf cpf, Password password, List<Role> roles) {
        this.cpf = cpf;
        this.password = password;
        this.roles = roles;
    }

    public List<String> getRolesFormattedAsString() {
        return roles.stream().map(role -> role.getName().name()).toList();
    }
}
