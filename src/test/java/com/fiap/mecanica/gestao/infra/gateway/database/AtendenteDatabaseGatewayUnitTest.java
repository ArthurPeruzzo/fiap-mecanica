package com.fiap.mecanica.gestao.infra.gateway.database;

import com.fiap.mecanica.gestao.infra.gateway.entity.AtendenteEntity;
import com.fiap.mecanica.gestao.infra.gateway.repository.AtendenteRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AtendenteDatabaseGatewayUnitTest {

    @InjectMocks
    private AtendenteDatabaseGateway gateway;

    @Mock
    private AtendenteRepository atendenteRepository;

    @Test
    void findById_shouldReturnMappedAtendenteWhenFound() {
        var entity = AtendenteEntity.builder()
                .id(1L)
                .nome("Ana")
                .sobrenome("Costa")
                .userId(10L)
                .build();

        Mockito.when(atendenteRepository.findById(1L)).thenReturn(Optional.of(entity));

        var result = gateway.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Ana", result.get().getNomeCompleto().nome());
        assertEquals("Costa", result.get().getNomeCompleto().sobrenome());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        Mockito.when(atendenteRepository.findById(99L)).thenReturn(Optional.empty());

        var result = gateway.findById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(atendenteRepository.findById(Mockito.anyLong()))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.findById(1L));
    }
}
