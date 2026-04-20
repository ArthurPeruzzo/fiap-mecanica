package com.fiap.mecanica.gestao.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Veiculo {
	private Long id;
	private String placa;
	private String modelo;
	private String ano;
}
