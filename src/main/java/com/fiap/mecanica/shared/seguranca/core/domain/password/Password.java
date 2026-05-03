package com.fiap.mecanica.shared.seguranca.core.domain.password;

import java.util.regex.Pattern;

public final class Password extends PasswordBase {
    private static final String REGEX_VALIDATE = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$";

    private static final String MENSAGEM_FORMATO_INVALIDO = """
            O formato da senha não é válido. A senha deve seguir as seguintes especificações:
            - Tenha oito caracteres ou mais
            - Incluir uma letra maiúscula
            - Use pelo menos uma letra minúscula
            - Consista em pelo menos um dígito
            - Precisa ter um símbolo especial (por exemplo: @, #, $, %, etc.)
            - Não conter espaços
            """;

    public Password(String value) {
        super(validar(value));
    }

    private static String validar(String value) {
        if (!Pattern.compile(REGEX_VALIDATE).matcher(value).matches()) {
            throw new IllegalArgumentException(MENSAGEM_FORMATO_INVALIDO);
        }
        return value;
    }
}
