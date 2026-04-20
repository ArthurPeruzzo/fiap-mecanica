package com.fiap.mecanica.gestao.infra.controller.validation;

import com.fiap.mecanica.gestao.infra.controller.json.ClienteRequestJson;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DocumentoValidoValidator implements ConstraintValidator<DocumentoValido, ClienteRequestJson> {

    @Override
    public boolean isValid(ClienteRequestJson req, ConstraintValidatorContext ctx) {
        boolean temCpf  = req.cpf()  != null && !req.cpf().isBlank();
        boolean temCnpj = req.cnpj() != null && !req.cnpj().isBlank();
        return temCpf ^ temCnpj;
    }
}
