package com.fiap.mecanica.gestao.infra.controller.json;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.shared.valueobjects.Cnpj;
import com.fiap.mecanica.shared.valueobjects.Cpf;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ClienteResponseJson", description = "Dados do cliente")
public record ClienteResponseJson(

        @Schema(description = "ID do cliente", example = "1")
        Long id,

        @Schema(description = "Nome do cliente", example = "Pedro")
        String nome,

        @Schema(description = "CPF do cliente (somente dígitos)", example = "18825469039")
        String cpf,

        @Schema(description = "CNPJ do cliente (somente caracteres do documento)", example = "9BX1W34S000144")
        String cnpj
) {
    public static ClienteResponseJson from(Cliente cliente) {
        return new ClienteResponseJson(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf().map(Cpf::getValorFormatado).orElse(null),
                cliente.getCnpj().map(Cnpj::getValorFormatado).orElse(null)
        );
    }
}
