package com.fiap.mecanica.ordemdeservico.infra.controller.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(name = "ServicoVinculadoResponseJson", description = "Serviço vinculado à ordem de serviço")
public record ServicoVinculadoResponseJson(

        @Schema(description = "ID do serviço cadastrado no sistema", example = "3")
        Long servicoId,

        @Schema(description = "Preço cobrado pelo serviço no momento do diagnóstico", example = "150.00")
        BigDecimal preco,

        @Schema(description = "Status de execução do serviço", example = "EM_EXECUCAO",
                allowableValues = {"NAO_INICIADO", "EM_EXECUCAO", "FINALIZADO"})
        String status,

        @Schema(description = "Data e hora de início da execução")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss", locale = "pt-BR", timezone = "America/Sao_Paulo")
        LocalDateTime dataInicioExecucao,

        @Schema(description = "Data e hora de conclusão da execução")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss", locale = "pt-BR", timezone = "America/Sao_Paulo")
        LocalDateTime dataFimExecucao
) {
}
