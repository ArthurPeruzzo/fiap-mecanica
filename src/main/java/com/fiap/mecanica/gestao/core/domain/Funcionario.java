package com.fiap.mecanica.gestao.core.domain;

import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public abstract class Funcionario {
	private Long id;
	private NomeCompleto nomeCompleto;
}
