package com.fiap.mecanica.shared.metricas.infra.gateway;

import com.fiap.mecanica.shared.metricas.core.domain.FaseOrdemDeServico;
import com.fiap.mecanica.shared.metricas.core.gateway.MetricasGateway;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;

@Component
public class MetricasMicrometerGateway implements MetricasGateway {

    private static final String METRICA_OS_CRIADAS = "os.criadas";
    private static final String METRICA_OS_DURACAO = "os.duracao";
    private static final String TAG_FASE = "fase";

    private final MeterRegistry meterRegistry;

    public MetricasMicrometerGateway(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void registrarOrdemCriada() {
        meterRegistry.counter(METRICA_OS_CRIADAS).increment();
    }

    @Override
    public void registrarDuracaoFase(FaseOrdemDeServico fase, Duration duracao) {
        if (duracao == null || duracao.isNegative()) return;
        meterRegistry.timer(METRICA_OS_DURACAO, TAG_FASE, fase.name().toLowerCase(Locale.ROOT))
                .record(duracao);
    }
}
