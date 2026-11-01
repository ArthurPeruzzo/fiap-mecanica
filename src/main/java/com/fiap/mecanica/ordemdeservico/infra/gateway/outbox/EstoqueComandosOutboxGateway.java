package com.fiap.mecanica.ordemdeservico.infra.gateway.outbox;

import com.fiap.mecanica.ordemdeservico.core.gateway.EstoqueComandosGateway;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OutboxEventoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OutboxEventoRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Grava o comando na outbox. Não abre transação própria: participa da que o caso de uso abriu, para que a
 * mudança de status da OS e o comando sejam confirmados (ou descartados) juntos.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EstoqueComandosOutboxGateway implements EstoqueComandosGateway {

    static final String TIPO_LIBERAR_RESERVA = "LIBERAR_RESERVA";

    private final OutboxEventoRepository outboxEventoRepository;

    @Override
    public void solicitarLiberacaoDeReservas(Long ordemServicoId) {
        try {
            outboxEventoRepository.save(OutboxEventoEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .tipo(TIPO_LIBERAR_RESERVA)
                    .agregadoId(ordemServicoId)
                    .payload("{\"ordemServicoId\":" + ordemServicoId + "}")
                    .criadoEm(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Erro ao registrar comando de liberacao de reservas da ordem de servico {}", ordemServicoId, e);
            throw new ErroAcessoBaseDeDadosException();
        }
    }
}
