package com.fiap.mecanica.gestao.infra.gateway.database;

import com.fiap.mecanica.gestao.infra.gateway.entity.MecanicoEntity;
import com.fiap.mecanica.gestao.infra.gateway.repository.MecanicoRepository;
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
class MecanicoDatabaseGatewayUnitTest {

    @InjectMocks
    private MecanicoDatabaseGateway gateway;

    @Mock
    private MecanicoRepository mecanicoRepository;

    @Test
    void findById_shouldReturnMappedMecanicoWhenFound() {
        var entity = MecanicoEntity.builder()
                .id(1L)
                .nome("Carlos")
                .sobrenome("Souza")
                .especialidade("Motor")
                .userId(20L)
                .build();

        Mockito.when(mecanicoRepository.findById(1L)).thenReturn(Optional.of(entity));

        var result = gateway.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Carlos", result.get().getNomeCompleto().nome());
        assertEquals("Souza", result.get().getNomeCompleto().sobrenome());
        assertEquals("Motor", result.get().getEspecialidade());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        Mockito.when(mecanicoRepository.findById(99L)).thenReturn(Optional.empty());

        var result = gateway.findById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(mecanicoRepository.findById(Mockito.anyLong()))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.findById(1L));
    }
}
