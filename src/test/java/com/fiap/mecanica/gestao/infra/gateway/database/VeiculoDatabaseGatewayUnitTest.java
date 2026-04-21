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
    // buscarPorId
    // -------------------------------------------------------------------------

    @Test
    void buscarPorId_shouldReturnMappedVeiculoWhenFound() {
        var entity = VeiculoEntity.builder().id(1L).clienteId(2L).placa("ABC1234").modelo("Gol").ano(2020).build();
        Mockito.when(veiculoRepository.findById(1L)).thenReturn(java.util.Optional.of(entity));

        var result = gateway.buscarPorId(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("ABC1234", result.get().getPlaca().getValor());
    }

    @Test
    void buscarPorId_shouldReturnEmptyWhenNotFound() {
        Mockito.when(veiculoRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertTrue(gateway.buscarPorId(99L).isEmpty());
    }

    @Test
    void buscarPorId_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(veiculoRepository.findById(Mockito.anyLong()))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.buscarPorId(1L));
    }

    // -------------------------------------------------------------------------
    // atualizar
    // -------------------------------------------------------------------------

    @Test
    void atualizar_shouldSaveEntityWithId() {
        var veiculo = Veiculo.reconstituir(1L, 2L, "ABC1D23", "Onix", 2023);
        var captor = ArgumentCaptor.forClass(VeiculoEntity.class);

        gateway.atualizar(veiculo);

        Mockito.verify(veiculoRepository).save(captor.capture());
        var entity = captor.getValue();
        assertEquals(1L, entity.getId());
        assertEquals(2L, entity.getClienteId());
        assertEquals("ABC1D23", entity.getPlaca());
        assertEquals("Onix", entity.getModelo());
        assertEquals(2023, entity.getAno());
    }

    @Test
    void atualizar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        var veiculo = Veiculo.reconstituir(1L, 2L, "ABC1234", "Gol", 2020);
        Mockito.when(veiculoRepository.save(Mockito.any())).thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.atualizar(veiculo));
    }

    // -------------------------------------------------------------------------
    // existePorPlacaExcluindoId
    // -------------------------------------------------------------------------

    @Test
    void existePorPlacaExcluindoId_shouldReturnTrueWhenAnotherVeiculoHasPlaca() {
        Mockito.when(veiculoRepository.existsByPlacaAndIdNot("ABC1234", 1L)).thenReturn(true);

        assertTrue(gateway.existePorPlacaExcluindoId("ABC1234", 1L));
    }

    @Test
    void existePorPlacaExcluindoId_shouldReturnFalseWhenOnlyCurrentVeiculoHasPlaca() {
        Mockito.when(veiculoRepository.existsByPlacaAndIdNot("ABC1234", 1L)).thenReturn(false);

        assertFalse(gateway.existePorPlacaExcluindoId("ABC1234", 1L));
    }

    @Test
    void existePorPlacaExcluindoId_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(veiculoRepository.existsByPlacaAndIdNot(Mockito.anyString(), Mockito.anyLong()))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.existePorPlacaExcluindoId("ABC1234", 1L));
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
