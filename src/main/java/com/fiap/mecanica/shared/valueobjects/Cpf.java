package com.fiap.mecanica.shared.valueobjects;

public class Cpf implements Documento {
	private final String valor;

	public Cpf(String value) {
		this.valor = value.replaceAll("[.\\-/]", "");
	}

	@Override
	public String getValor() {
		return valor;
	}
}
