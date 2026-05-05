package com.fiap.mecanica.gestao.infra.controller.json;

import com.fiap.mecanica.gestao.infra.controller.validation.DocumentoValido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

@Schema(
		name = "ClienteRequestJson",
		description = "Dados necessários para criar um cliente"
)
@DocumentoValido
public record ClienteRequestJson(
		@Schema(
				description = "Nome do cliente",
				example = "Pedro"
		)
		@NotBlank(message = "O nome deve ser preenchido")
		String nome,

		@Schema(
				description = "CPF do cliente pessoa física. Exemplo: 123.456.789-09 com ou sem formatação",
				example = "123.456.789-09"
		)
		@CPF(message = "O conteúdo ou a formatação do CPF não é válida")
		String cpf,

		@Schema(
				description = "CNPJ do cliente pessoa jurídica. Segue formatos de exemplo: AA.AAA.AAA/AAAA-DV ou 00.000.000/0000-00 com ou sem formatação. CNPJ alfanuméricos são aceitos",
				example = "1A.3BC.45D/0001-EF"
		)
		@CNPJ(message = "O conteúdo ou a formatação do CNPJ não é válida. Segue formatos de exemplo: AA.AAA.AAA/AAAA-DV ou 00.000.000/0000-00 com ou sem formatação", format = CNPJ.Format.ALPHANUMERIC)
		String cnpj) {
}
