package com.fiap.mecanica.gestao.infra.controller.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = PlacaValidaValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PlacaValida {
    String message() default "A placa informada é inválida. Formatos aceitos: ABC1234 (antiga) ou ABC1D23 (Mercosul), com ou sem hífen";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
