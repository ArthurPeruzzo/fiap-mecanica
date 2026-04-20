package com.fiap.mecanica.gestao.core.domain;

import com.fiap.mecanica.shared.valueobjects.Cnpj;
import com.fiap.mecanica.shared.valueobjects.Cpf;
import com.fiap.mecanica.shared.valueobjects.Documento;
import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
import lombok.Getter;

import java.util.Optional;

@Getter
public class Cliente {
	private Long id;
	private NomeCompleto nomeCompleto;
	private Documento documento;

	public Cliente(NomeCompleto nomeCompleto, String cnpj, String cpf) {
		this.nomeCompleto = nomeCompleto;

		boolean temCnpj = cnpj != null;
		boolean temCpf  = cpf  != null;

		if (temCpf == temCnpj) {
			throw new IllegalArgumentException("O cnpj ou cpf precisam estar preenchidos");
		}

		if (cnpj != null) {
			documento = new Cnpj(cnpj);
		} else {
			documento = new Cpf(cpf);
		}
	}

	public void atualizar(NomeCompleto nomeCompleto, String cnpj, String cpf) {
		boolean temCnpj = cnpj != null;
		boolean temCpf  = cpf  != null;

		if (temCpf == temCnpj) {
			throw new IllegalArgumentException("O cnpj ou cpf precisam estar preenchidos");
		}

		this.nomeCompleto = nomeCompleto;
		this.documento = cnpj != null ? new Cnpj(cnpj) : new Cpf(cpf);
	}

	public static Cliente reconstituir(Long id, NomeCompleto nomeCompleto, String cnpj, String cpf) {
		var cliente = new Cliente(nomeCompleto, cnpj, cpf);
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
