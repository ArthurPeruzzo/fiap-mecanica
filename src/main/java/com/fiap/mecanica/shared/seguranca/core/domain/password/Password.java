package com.fiap.mecanica.shared.seguranca.core.domain.password;

import java.util.regex.Pattern;

public final class Password extends PasswordBase {
    private static final String REGEX_VALIDATE = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$";

    private static final String PASSWORD_REQUIREMENTS = """
            O formato da senha não é válido. A senha deve seguir as seguintes especificações:
            - Tenha oito caracteres ou mais
            - Incluir uma letra maiúscula
            - Use pelo menos uma letra minúscula
            - Consista em pelo menos um dígito
            - Precisa ter um símbolo especial (por exemplo: @, #, $, %, etc.)
            - Não conter espaços
            """;

    public Password(String value) {
        boolean isValid = Pattern.compile(REGEX_VALIDATE, Pattern.CASE_INSENSITIVE)
                .matcher(value)
                .matches();

        if (!isValid) {
            throw new IllegalArgumentException(PASSWORD_REQUIREMENTS);
        }

        super(value);
    }
}
