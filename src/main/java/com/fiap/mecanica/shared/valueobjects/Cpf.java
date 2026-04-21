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

	public String getValorFormatado() {
		return valor.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
	}
}
