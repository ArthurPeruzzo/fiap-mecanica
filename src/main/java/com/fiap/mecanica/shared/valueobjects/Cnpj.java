package com.fiap.mecanica.shared.valueobjects;

public class Cnpj implements Documento {
	private final String valor;

	public Cnpj(String value) {
		this.valor = value.replaceAll("[.\\-/]", "");
	}

	@Override
	public String getValor() {
		return valor;
	}

	public String getValorFormatado() {
		return valor.replaceAll("(.{2})(.{3})(.{3})(.{4})(.{2})", "$1.$2.$3/$4-$5");
	}
}
