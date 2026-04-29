package com.fiap.mecanica.estoque.infra.gateway.entity;

import com.fiap.mecanica.estoque.core.domain.UnidadeMedida;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name="insumo")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class InsumoEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nome", nullable = false)
	private String nome;

	@Column(name = "descricao", nullable = false)
	private String descricao;

	@Column(name = "preco", nullable = false)
	private BigDecimal preco;

	@Column(name = "quantidade_estoque", nullable = false)
	private Integer quantidadeEstoque;

	@Column(name = "unidade_medida", nullable = false)
	@Enumerated(EnumType.STRING)
	private UnidadeMedida unidadeMedida;

}
