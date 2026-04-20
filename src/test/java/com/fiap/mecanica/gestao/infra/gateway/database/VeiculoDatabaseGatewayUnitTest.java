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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

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

    // -------------------------------------------------------------------------
    // listar
    // -------------------------------------------------------------------------

    @Test
    void listar_shouldReturnMappedPagina() {
        var entity = VeiculoEntity.builder().id(1L).clienteId(2L).placa("ABC1234").modelo("Gol").ano(2020).build();
        var springPage = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        Mockito.when(veiculoRepository.findAll(PageRequest.of(0, 10))).thenReturn(springPage);

        var resultado = gateway.listar(0, 10);

        assertEquals(1, resultado.content().size());
        assertEquals(1L, resultado.totalElements());
        var veiculo = resultado.content().getFirst();
        assertEquals(1L, veiculo.getId());
        assertEquals(2L, veiculo.getClienteId());
        assertEquals("ABC1234", veiculo.getPlaca().getValor());
        assertEquals("Gol", veiculo.getModelo());
        assertEquals(2020, veiculo.getAno());
    }

    @Test
    void listar_shouldReturnEmptyPaginaWhenNoResults() {
        var springPage = new PageImpl<VeiculoEntity>(List.of(), PageRequest.of(0, 10), 0);
        Mockito.when(veiculoRepository.findAll(PageRequest.of(0, 10))).thenReturn(springPage);

        var resultado = gateway.listar(0, 10);

        assertTrue(resultado.content().isEmpty());
        assertEquals(0L, resultado.totalElements());
    }

    @Test
    void listar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(veiculoRepository.findAll(Mockito.any(PageRequest.class)))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.listar(0, 10));
    }
}
