package com.fiap.mecanica.gestao.core.domain;

import com.fiap.mecanica.gestao.core.exception.DocumentoInvalidoException;
import com.fiap.mecanica.shared.valueobjects.Cnpj;
import com.fiap.mecanica.shared.valueobjects.Cpf;
import com.fiap.mecanica.shared.valueobjects.Documento;
import lombok.Getter;

import java.util.Optional;

@Getter
public class Cliente {
	private Long id;
	private String nome;
	private Documento documento;

	public Cliente(String nome, String cnpj, String cpf) {
		this.nome = nome;

		boolean temCnpj = cnpj != null;
		boolean temCpf  = cpf  != null;

		if (temCpf == temCnpj) {
			throw new DocumentoInvalidoException();
		}

		if (cnpj != null) {
			documento = new Cnpj(cnpj);
		} else {
			documento = new Cpf(cpf);
		}
	}

	public void atualizar(String nome, String cnpj, String cpf) {
		boolean temCnpj = cnpj != null;
		boolean temCpf  = cpf  != null;

		if (temCpf == temCnpj) {
			throw new DocumentoInvalidoException();
		}

		this.nome = nome;
		this.documento = cnpj != null ? new Cnpj(cnpj) : new Cpf(cpf);
	}

	public static Cliente reconstituir(Long id, String nome, String cnpj, String cpf) {
		var cliente = new Cliente(nome, cnpj, cpf);
		cliente.id = id;
		return cliente;
	}

	public Optional<Cnpj> getCnpj() {
		if (documento instanceof Cnpj cnpj) {
			return Optional.of(cnpj);
		}
		return Optional.empty();
	}

	public Optional<Cpf> getCpf() {
		if (documento instanceof Cpf cpf) {
			return Optional.of(cpf);
		}
		return Optional.empty();
	}
}
