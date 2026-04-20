package com.fiap.mecanica.gestao.infra.controller.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PlacaValidaValidator implements ConstraintValidator<PlacaValida, String> {

    private static final Pattern PLACA_ANTIGA   = Pattern.compile("^[A-Z]{3}\\d{4}$");
    private static final Pattern PLACA_MERCOSUL = Pattern.compile("^[A-Z]{3}\\d[A-Z]\\d{2}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null || value.isBlank()) return true;
        String normalizada = value.replace("-", "").toUpperCase();
        return PLACA_ANTIGA.matcher(normalizada).matches()
                || PLACA_MERCOSUL.matcher(normalizada).matches();
    }
}
