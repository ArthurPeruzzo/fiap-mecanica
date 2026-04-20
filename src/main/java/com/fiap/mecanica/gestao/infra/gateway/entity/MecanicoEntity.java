package com.fiap.mecanica.gestao.infra.gateway.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="mecanico")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MecanicoEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nome", nullable = false)
	private String nome;

	@Column(name = "sobrenome", nullable = false)
	private String sobrenome;

	@Column(name = "especialidade", nullable = false)
	private String especialidade;

	@Column(name = "user_id", nullable = false)
	private Long userId;
}
