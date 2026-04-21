package com.fiap.mecanica.shared.valueobjects;

public record NomeCompleto(String nome, String sobrenome){
	public String nomeCompleto() {
		return nome + " " + sobrenome;
	}
}
