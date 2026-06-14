package com.fiap.mecanica.ordemdeservico.infra.controller.json;

import com.fiap.mecanica.ordemdeservico.core.dto.ConsultarStatusOrdemDeServicoDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "StatusOrdemDeServicoResponseJson", description = "Status atual de uma ordem de serviço")
public record StatusOrdemDeServicoResponseJson(
        @Schema(description = "ID da ordem de serviço", example = "1")
        Long id,
        @Schema(description = "Status atual da ordem de serviço", example = "EM_DIAGNOSTICO",
                allowableValues = {"RECEBIDA", "EM_DIAGNOSTICO", "DIAGNOSTICO_CONCLUIDO",
                        "AGUARDANDO_APROVACAO", "EM_EXECUCAO", "FINALIZADA", "ENTREGUE", "CANCELADA"})
        String status
) {
    public static StatusOrdemDeServicoResponseJson from(ConsultarStatusOrdemDeServicoDto dto) {
        return new StatusOrdemDeServicoResponseJson(dto.id(), dto.status());
    }
}
