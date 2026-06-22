package com.fiap.mecanica.ordemdeservico.core.domain;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.LinkAprovacaoOrcamento;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LinkAprovacaoOrcamentoUnitTest {

    private static final Long ORDEM_ID = 1L;

    @Test
    void shouldGerarTokenUUIDAoCriar() {
        LinkAprovacaoOrcamento link = new LinkAprovacaoOrcamento(ORDEM_ID);

        assertNotNull(link.getToken());
        assertDoesNotThrow(() -> UUID.fromString(link.getToken()));
    }

    @Test
    void shouldDefinirExpiracaoEm3DiasAoCriar() {
        LocalDateTime antes = LocalDateTime.now().plusDays(3).minusSeconds(1);
        LinkAprovacaoOrcamento link = new LinkAprovacaoOrcamento(ORDEM_ID);
        LocalDateTime depois = LocalDateTime.now().plusDays(3).plusSeconds(1);

        assertTrue(link.getDataExpiracao().isAfter(antes));
        assertTrue(link.getDataExpiracao().isBefore(depois));
    }

    @Test
    void shouldEstarValidoQuandoNaoUtilizadoENaoExpirado() {
        LinkAprovacaoOrcamento link = new LinkAprovacaoOrcamento(ORDEM_ID);

        assertTrue(link.estaValido());
    }

    @Test
    void shouldNaoEstarValidoQuandoUtilizado() {
        LinkAprovacaoOrcamento link = new LinkAprovacaoOrcamento(ORDEM_ID);
        link.marcarComoUtilizado();

        assertFalse(link.estaValido());
    }

    @Test
    void shouldNaoEstarValidoQuandoExpirado() {
        LocalDateTime expiracaoPassada = LocalDateTime.now().minusDays(1);
        LinkAprovacaoOrcamento link = LinkAprovacaoOrcamento.builder()
                .id(1L)
                .ordemDeServicoId(ORDEM_ID)
                .token(UUID.randomUUID().toString())
                .dataExpiracao(expiracaoPassada)
                .build();

        assertFalse(link.estaValido());
    }

    @Test
    void shouldDefinirDataUtilizacaoAoMarcarComoUtilizado() {
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);
        LinkAprovacaoOrcamento link = new LinkAprovacaoOrcamento(ORDEM_ID);
        link.marcarComoUtilizado();

        assertNotNull(link.getDataUtilizacao());
        assertTrue(link.getDataUtilizacao().isAfter(antes));
    }

    @Test
    void shouldGerarTokensDiferentesParaCadaInstancia() {
        LinkAprovacaoOrcamento link1 = new LinkAprovacaoOrcamento(ORDEM_ID);
        LinkAprovacaoOrcamento link2 = new LinkAprovacaoOrcamento(ORDEM_ID);

        assertNotEquals(link1.getToken(), link2.getToken());
    }
}
