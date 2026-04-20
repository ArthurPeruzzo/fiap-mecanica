package com.fiap.mecanica.gestao.core.domain;

import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Mecanico {
	private Long id;
	private NomeCompleto nomeCompleto;
	private String especialidade;
}
