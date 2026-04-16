package com.fiap.mecanica.gestao.core.domain;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class Atendente extends Persona {
	private String telefone;
}
