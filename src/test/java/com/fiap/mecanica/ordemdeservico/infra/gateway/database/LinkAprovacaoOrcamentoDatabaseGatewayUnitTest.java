package com.fiap.mecanica.ordemdeservico.infra.gateway.database;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.LinkAprovacaoOrcamento;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.LinkAprovacaoOrcamentoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.LinkAprovacaoOrcamentoRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LinkAprovacaoOrcamentoDatabaseGatewayUnitTest {

    @InjectMocks
    private LinkAprovacaoOrcamentoDatabaseGateway gateway;

    @Mock
    private LinkAprovacaoOrcamentoRepository repository;

    private static final Long LINK_ID = 1L;
    private static final Long ORDEM_ID = 10L;
    private static final String TOKEN = UUID.randomUUID().toString();
    private static final LocalDateTime DATA_EXPIRACAO = LocalDateTime.now().plusDays(3);
    private static final LocalDateTime DATA_UTILIZACAO = LocalDateTime.now();

    private LinkAprovacaoOrcamentoEntity entityPadrao() {
        return LinkAprovacaoOrcamentoEntity.builder()
                .id(LINK_ID)
                .ordemServicoId(ORDEM_ID)
                .token(TOKEN)
                .dataExpiracao(DATA_EXPIRACAO)
                .dataUtilizacao(null)
                .build();
    }

    @Test
    void salvar_shouldSaveEntityWithCorrectFields() {
        LinkAprovacaoOrcamento link = new LinkAprovacaoOrcamento(ORDEM_ID);
        var captor = ArgumentCaptor.forClass(LinkAprovacaoOrcamentoEntity.class);

        gateway.salvar(link);

        Mockito.verify(repository).save(captor.capture());
        var entity = captor.getValue();
        assertNull(entity.getId());
        assertEquals(ORDEM_ID, entity.getOrdemServicoId());
        assertEquals(link.getToken(), entity.getToken());
        assertNotNull(entity.getDataExpiracao());
        assertNull(entity.getDataUtilizacao());
    }

    @Test
    void salvar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(repository.save(Mockito.any())).thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class,
                () -> gateway.salvar(new LinkAprovacaoOrcamento(ORDEM_ID)));
    }

    @Test
    void buscarPorToken_shouldReturnMappedLinkWhenFound() {
        Mockito.when(repository.findByToken(TOKEN)).thenReturn(Optional.of(entityPadrao()));

        Optional<LinkAprovacaoOrcamento> result = gateway.buscarPorToken(TOKEN);

        assertTrue(result.isPresent());
        var link = result.get();
        assertEquals(LINK_ID, link.getId());
        assertEquals(ORDEM_ID, link.getOrdemDeServicoId());
        assertEquals(TOKEN, link.getToken());
        assertEquals(DATA_EXPIRACAO, link.getDataExpiracao());
        assertNull(link.getDataUtilizacao());
    }

    @Test
    void buscarPorToken_shouldMapDataUtilizacaoWhenPresent() {
        var entityUtilizado = LinkAprovacaoOrcamentoEntity.builder()
                .id(LINK_ID).ordemServicoId(ORDEM_ID).token(TOKEN)
                .dataExpiracao(DATA_EXPIRACAO).dataUtilizacao(DATA_UTILIZACAO)
                .build();
        Mockito.when(repository.findByToken(TOKEN)).thenReturn(Optional.of(entityUtilizado));

        var link = gateway.buscarPorToken(TOKEN).orElseThrow();

        assertEquals(DATA_UTILIZACAO, link.getDataUtilizacao());
    }

    @Test
    void buscarPorToken_shouldReturnEmptyWhenNotFound() {
        Mockito.when(repository.findByToken(TOKEN)).thenReturn(Optional.empty());

        assertTrue(gateway.buscarPorToken(TOKEN).isEmpty());
    }

    @Test
    void buscarPorToken_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(repository.findByToken(Mockito.any())).thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class,
                () -> gateway.buscarPorToken(TOKEN));
    }

    @Test
    void atualizar_shouldSaveEntityWithIdAndAllFields() {
        LinkAprovacaoOrcamento link = LinkAprovacaoOrcamento.builder()
                .id(LINK_ID).ordemDeServicoId(ORDEM_ID).token(TOKEN)
                .dataExpiracao(DATA_EXPIRACAO).dataUtilizacao(null)
                .build();
        var captor = ArgumentCaptor.forClass(LinkAprovacaoOrcamentoEntity.class);

        gateway.atualizar(link);

        Mockito.verify(repository).save(captor.capture());
        var entity = captor.getValue();
        assertEquals(LINK_ID, entity.getId());
        assertEquals(ORDEM_ID, entity.getOrdemServicoId());
        assertEquals(TOKEN, entity.getToken());
        assertEquals(DATA_EXPIRACAO, entity.getDataExpiracao());
        assertNull(entity.getDataUtilizacao());
    }

    @Test
    void atualizar_shouldPersistDataUtilizacaoQuandoMarcadoComoUtilizado() {
        LinkAprovacaoOrcamento link = LinkAprovacaoOrcamento.builder()
                .id(LINK_ID).ordemDeServicoId(ORDEM_ID).token(TOKEN)
                .dataExpiracao(DATA_EXPIRACAO).dataUtilizacao(DATA_UTILIZACAO)
                .build();
        var captor = ArgumentCaptor.forClass(LinkAprovacaoOrcamentoEntity.class);

        gateway.atualizar(link);

        Mockito.verify(repository).save(captor.capture());
        assertEquals(DATA_UTILIZACAO, captor.getValue().getDataUtilizacao());
    }

    @Test
    void atualizar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        LinkAprovacaoOrcamento link = LinkAprovacaoOrcamento.builder()
                .id(LINK_ID).ordemDeServicoId(ORDEM_ID).token(TOKEN)
                .dataExpiracao(DATA_EXPIRACAO).dataUtilizacao(DATA_UTILIZACAO)
                .build();
        Mockito.when(repository.save(Mockito.any())).thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.atualizar(link));
    }
}
