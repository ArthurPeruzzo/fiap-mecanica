package com.fiap.mecanica.gestao.core.domain;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class Persona {
	private Long id;
	private String nome;
	private String sobrenome;
}
