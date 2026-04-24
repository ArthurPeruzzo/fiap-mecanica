package com.fiap.mecanica.ordemdeservico.infra.gateway.database;

import com.fiap.mecanica.ordemdeservico.core.domain.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OrdemDeServicoRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrdemDeServicoDatabaseGatewayUnitTest {

    @InjectMocks
    private OrdemDeServicoDatabaseGateway gateway;

    @Mock
    private OrdemDeServicoRepository ordemDeServicoRepository;

    @Test
    void criar_shouldSaveEntityWithAllFields() {
        var os = new OrdemDeServico(1L, 2L, 3L);
        var captor = ArgumentCaptor.forClass(OrdemDeServicoEntity.class);

        gateway.criar(os);

        Mockito.verify(ordemDeServicoRepository).save(captor.capture());
        var entity = captor.getValue();
        assertEquals(1L, entity.getClienteId());
        assertEquals(2L, entity.getVeiculoId());
        assertEquals(3L, entity.getAtendenteId());
        assertEquals(StatusOrdemDeServico.RECEBIDA, entity.getStatus());
        assertNotNull(entity.getDataCriacao());
    }

    @Test
    void criar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(ordemDeServicoRepository.save(Mockito.any()))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class,
                () -> gateway.criar(new OrdemDeServico(1L, 2L, 3L)));
    }
}
