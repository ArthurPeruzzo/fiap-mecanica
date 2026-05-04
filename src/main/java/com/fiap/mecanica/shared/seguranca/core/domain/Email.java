package com.fiap.mecanica.shared.seguranca.core.domain;

import java.io.Serializable;
import java.util.regex.Pattern;

public record Email(String value) implements Serializable {

    private static final Pattern REGEX_VALIDATE = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        boolean isValid = REGEX_VALIDATE
                .matcher(value)
                .matches();

        if (!isValid) {
            throw new IllegalArgumentException("O formato do email não é válido. Deve ser seguido o seguinte formato: exemplo@exemplo.com");
        }

    }

}
