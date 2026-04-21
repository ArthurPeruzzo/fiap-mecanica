package com.fiap.mecanica.gestao.infra.gateway.entity;

import com.fiap.mecanica.gestao.core.domain.Turno;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="atendente")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AtendenteEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nome", nullable = false)
	private String nome;

	@Column(name = "sobrenome", nullable = false)
	private String sobrenome;

	@Column(name = "turno", nullable = false)
	@Enumerated(EnumType.STRING)
	private Turno turno;

	@Column(name = "user_id", nullable = false)
	private Long userId;
}
