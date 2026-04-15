package com.fiap.mecanica.shared.seguranca.core.domain.password;

import lombok.Getter;

import java.io.Serializable;

@Getter
public abstract class PasswordBase implements Serializable {
    private final String value;

    protected PasswordBase(String value) {
        this.value = value;
    }

}
