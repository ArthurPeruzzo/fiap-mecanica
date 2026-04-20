package com.fiap.mecanica.gestao.infra.gateway.database;

import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.gestao.infra.gateway.entity.VeiculoEntity;
import com.fiap.mecanica.gestao.infra.gateway.repository.VeiculoRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class VeiculoDatabaseGatewayUnitTest {

    @InjectMocks
    private VeiculoDatabaseGateway gateway;

    @Mock
    private VeiculoRepository veiculoRepository;

    // -------------------------------------------------------------------------
    // criar
    // -------------------------------------------------------------------------

    @Test
    void criar_shouldSaveEntityWithCorrectFields() {
        var veiculo = new Veiculo(1L, "ABC1234", "Gol", 2020);
        var captor = ArgumentCaptor.forClass(VeiculoEntity.class);

        gateway.criar(veiculo);

        Mockito.verify(veiculoRepository).save(captor.capture());
        var entity = captor.getValue();
        assertEquals(1L, entity.getClienteId());
        assertEquals("ABC1234", entity.getPlaca());
        assertEquals("Gol", entity.getModelo());
        assertEquals(2020, entity.getAno());
    }

    @Test
    void criar_shouldStripHyphenFromPlacaBeforeSaving() {
        var veiculo = new Veiculo(1L, "ABC-1234", "Gol", 2020);
        var captor = ArgumentCaptor.forClass(VeiculoEntity.class);

        gateway.criar(veiculo);

        Mockito.verify(veiculoRepository).save(captor.capture());
        assertEquals("ABC1234", captor.getValue().getPlaca());
    }

    @Test
    void criar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        var veiculo = new Veiculo(1L, "ABC1234", "Gol", 2020);
        Mockito.when(veiculoRepository.save(Mockito.any())).thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.criar(veiculo));
    }

    // -------------------------------------------------------------------------
    // existePorPlaca
    // -------------------------------------------------------------------------

    @Test
    void existePorPlaca_shouldReturnTrueWhenPlacaExists() {
        Mockito.when(veiculoRepository.existsByPlaca("ABC1234")).thenReturn(true);

        assertTrue(gateway.existePorPlaca("ABC1234"));
    }

    @Test
    void existePorPlaca_shouldReturnFalseWhenPlacaNotExists() {
        Mockito.when(veiculoRepository.existsByPlaca("ABC1234")).thenReturn(false);

        assertFalse(gateway.existePorPlaca("ABC1234"));
    }

    @Test
    void existePorPlaca_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(veiculoRepository.existsByPlaca(Mockito.anyString()))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.existePorPlaca("ABC1234"));
    }
}
