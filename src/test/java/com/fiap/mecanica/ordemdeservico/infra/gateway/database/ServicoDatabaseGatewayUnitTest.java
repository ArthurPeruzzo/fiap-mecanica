package com.fiap.mecanica.ordemdeservico.infra.gateway.database;

import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.ServicoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.ServicoRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ServicoDatabaseGatewayUnitTest {

    @InjectMocks
    private ServicoDatabaseGateway gateway;

    @Mock
    private ServicoRepository servicoRepository;

    // -------------------------------------------------------------------------
    // criar
    // -------------------------------------------------------------------------

    @Test
    void criar_shouldSaveEntityWithCorrectFields() {
        var servico = new Servico("Troca de óleo", "Troca com filtro incluso", new BigDecimal("150.00"));
        var captor = ArgumentCaptor.forClass(ServicoEntity.class);

        gateway.criar(servico);

        Mockito.verify(servicoRepository).save(captor.capture());
        var entity = captor.getValue();
        assertEquals("Troca de óleo", entity.getNome());
        assertEquals("Troca com filtro incluso", entity.getDescricao());
        assertEquals(new BigDecimal("150.00"), entity.getPreco());
    }

    @Test
    void criar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        var servico = new Servico("Troca de óleo", "Desc", new BigDecimal("150.00"));
        Mockito.when(servicoRepository.save(Mockito.any())).thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.criar(servico));
    }

    // -------------------------------------------------------------------------
    // buscarPorId
    // -------------------------------------------------------------------------

    @Test
    void buscarPorId_shouldReturnMappedServicoWhenFound() {
        var entity = ServicoEntity.builder().id(1L).nome("Troca de óleo").descricao("Desc").preco(new BigDecimal("150.00")).build();
        Mockito.when(servicoRepository.findById(1L)).thenReturn(Optional.of(entity));

        var result = gateway.buscarPorId(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Troca de óleo", result.get().getNome());
        assertEquals(new BigDecimal("150.00"), result.get().getPreco());
    }

    @Test
    void buscarPorId_shouldReturnEmptyWhenNotFound() {
        Mockito.when(servicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(gateway.buscarPorId(99L).isEmpty());
    }

    @Test
    void buscarPorId_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(servicoRepository.findById(Mockito.anyLong())).thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.buscarPorId(1L));
    }

    // -------------------------------------------------------------------------
    // atualizar
    // -------------------------------------------------------------------------

    @Test
    void atualizar_shouldSaveEntityWithId() {
        var servico = Servico.reconstituir(1L, "Alinhamento", "Desc nova", new BigDecimal("200.00"));
        var captor = ArgumentCaptor.forClass(ServicoEntity.class);

        gateway.atualizar(servico);

        Mockito.verify(servicoRepository).save(captor.capture());
        var entity = captor.getValue();
        assertEquals(1L, entity.getId());
        assertEquals("Alinhamento", entity.getNome());
        assertEquals("Desc nova", entity.getDescricao());
        assertEquals(new BigDecimal("200.00"), entity.getPreco());
    }

    @Test
    void atualizar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        var servico = Servico.reconstituir(1L, "Nome", "Desc", new BigDecimal("100.00"));
        Mockito.when(servicoRepository.save(Mockito.any())).thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.atualizar(servico));
    }

    // -------------------------------------------------------------------------
    // deletar
    // -------------------------------------------------------------------------

    @Test
    void deletar_shouldCallDeleteById() {
        gateway.deletar(1L);

        Mockito.verify(servicoRepository).deleteById(1L);
    }

    @Test
    void deletar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.doThrow(new RuntimeException("db error")).when(servicoRepository).deleteById(Mockito.anyLong());

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.deletar(1L));
    }

    // -------------------------------------------------------------------------
    // listar
    // -------------------------------------------------------------------------

    @Test
    void listar_shouldReturnMappedPagina() {
        var entity = ServicoEntity.builder().id(1L).nome("Troca de óleo").descricao("Desc").preco(new BigDecimal("150.00")).build();
        var springPage = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        Mockito.when(servicoRepository.findAll(PageRequest.of(0, 10))).thenReturn(springPage);

        var resultado = gateway.listar(0, 10);

        assertEquals(1, resultado.content().size());
        assertEquals(1L, resultado.totalElements());
        assertEquals(1L, resultado.content().getFirst().getId());
        assertEquals("Troca de óleo", resultado.content().getFirst().getNome());
    }

    @Test
    void listar_shouldReturnEmptyPaginaWhenNoResults() {
        var springPage = new PageImpl<ServicoEntity>(List.of(), PageRequest.of(0, 10), 0);
        Mockito.when(servicoRepository.findAll(PageRequest.of(0, 10))).thenReturn(springPage);

        var resultado = gateway.listar(0, 10);

        assertTrue(resultado.content().isEmpty());
        assertEquals(0L, resultado.totalElements());
    }

    @Test
    void listar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(servicoRepository.findAll(Mockito.any(PageRequest.class))).thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.listar(0, 10));
    }
}
