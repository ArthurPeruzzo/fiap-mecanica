package com.fiap.mecanica.ordemdeservico.infra.controller.json;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Duration;

@Schema(name = "TempoMedioExecucaoResponseJson", description = "Tempo médio de execução dos serviços finalizados. Null quando nenhum serviço foi finalizado.")
public record TempoMedioExecucaoResponseJson(
        @Schema(description = "Componente de dias", example = "1") long dias,
        @Schema(description = "Componente de horas (0–23)", example = "4") long horas,
        @Schema(description = "Componente de minutos (0–59)", example = "30") long minutos
) {
    public static TempoMedioExecucaoResponseJson from(Duration duration) {
        if (duration == null) return null;
        return new TempoMedioExecucaoResponseJson(
                duration.toDaysPart(),
                duration.toHoursPart(),
                duration.toMinutesPart()
        );
    }
}
