package com.fiap.mecanica.gestao.infra.gateway.database;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.infra.gateway.entity.ClienteEntity;
import com.fiap.mecanica.gestao.infra.gateway.repository.ClienteRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ClienteDatabaseGatewayUnitTest {

    @InjectMocks
    private ClienteDatabaseGateway gateway;

    @Mock
    private ClienteRepository clienteRepository;

    // -------------------------------------------------------------------------
    // criar
    // -------------------------------------------------------------------------

    @Test
    void criar_shouldSaveEntityWithCpfAndNullCnpj() {
        var cliente = new Cliente(new NomeCompleto("Pedro", "Silva"), null, "12345678909");
        var captor = ArgumentCaptor.forClass(ClienteEntity.class);

        gateway.criar(cliente);

        Mockito.verify(clienteRepository).save(captor.capture());
        var entity = captor.getValue();
        assertEquals("Pedro", entity.getNome());
        assertEquals("Silva", entity.getSobrenome());
        assertEquals("12345678909", entity.getCpf());
        assertNull(entity.getCnpj());
    }

    @Test
    void criar_shouldSaveEntityWithCnpjAndNullCpf() {
        var cliente = new Cliente(new NomeCompleto("Empresa", "LTDA"), "00000000000191", null);
        var captor = ArgumentCaptor.forClass(ClienteEntity.class);

        gateway.criar(cliente);

        Mockito.verify(clienteRepository).save(captor.capture());
        var entity = captor.getValue();
        assertEquals("Empresa", entity.getNome());
        assertEquals("LTDA", entity.getSobrenome());
        assertEquals("00000000000191", entity.getCnpj());
        assertNull(entity.getCpf());
    }

    @Test
    void criar_shouldPropagateExceptionFromRepository() {
        var cliente = new Cliente(new NomeCompleto("Pedro", "Silva"), null, "12345678909");

        Mockito.when(clienteRepository.save(Mockito.any(ClienteEntity.class)))
                .thenThrow(new RuntimeException("erro no banco"));

        assertThrows(RuntimeException.class, () -> gateway.criar(cliente));
    }

    // -------------------------------------------------------------------------
    // buscarPorId
    // -------------------------------------------------------------------------

    @Test
    void buscarPorId_shouldReturnMappedClienteWhenFound() {
        var entity = ClienteEntity.builder().id(1L).nome("Pedro").sobrenome("Silva").cpf("12345678909").build();
        Mockito.when(clienteRepository.findById(1L)).thenReturn(java.util.Optional.of(entity));

        var result = gateway.buscarPorId(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Pedro", result.get().getNomeCompleto().nome());
        assertEquals("12345678909", result.get().getCpf().get().getValor());
    }

    @Test
    void buscarPorId_shouldReturnEmptyWhenNotFound() {
        Mockito.when(clienteRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertTrue(gateway.buscarPorId(99L).isEmpty());
    }

    @Test
    void buscarPorId_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(clienteRepository.findById(Mockito.anyLong()))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.buscarPorId(1L));
    }

    // -------------------------------------------------------------------------
    // atualizar
    // -------------------------------------------------------------------------

    @Test
    void atualizar_shouldSaveEntityWithId() {
        var cliente = Cliente.reconstituir(1L, new NomeCompleto("Carlos", "Costa"), null, "12345678909");
        var captor = ArgumentCaptor.forClass(ClienteEntity.class);

        gateway.atualizar(cliente);

        Mockito.verify(clienteRepository).save(captor.capture());
        var entity = captor.getValue();
        assertEquals(1L, entity.getId());
        assertEquals("Carlos", entity.getNome());
        assertEquals("12345678909", entity.getCpf());
        assertNull(entity.getCnpj());
    }

    @Test
    void atualizar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        var cliente = Cliente.reconstituir(1L, new NomeCompleto("Pedro", "Silva"), null, "12345678909");
        Mockito.when(clienteRepository.save(Mockito.any())).thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.atualizar(cliente));
    }

    // -------------------------------------------------------------------------
    // existePorCpfExcluindoId / existePorCnpjExcluindoId
    // -------------------------------------------------------------------------

    @Test
    void existePorCpfExcluindoId_shouldReturnTrueWhenAnotherClienteHasCpf() {
        Mockito.when(clienteRepository.existsByCpfAndIdNot("12345678909", 1L)).thenReturn(true);

        assertTrue(gateway.existePorCpfExcluindoId("12345678909", 1L));
    }

    @Test
    void existePorCpfExcluindoId_shouldReturnFalseWhenOnlyCurrentClienteHasCpf() {
        Mockito.when(clienteRepository.existsByCpfAndIdNot("12345678909", 1L)).thenReturn(false);

        assertFalse(gateway.existePorCpfExcluindoId("12345678909", 1L));
    }

    @Test
    void existePorCpfExcluindoId_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(clienteRepository.existsByCpfAndIdNot(Mockito.anyString(), Mockito.anyLong()))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.existePorCpfExcluindoId("12345678909", 1L));
    }

    @Test
    void existePorCnpjExcluindoId_shouldReturnTrueWhenAnotherClienteHasCnpj() {
        Mockito.when(clienteRepository.existsByCnpjAndIdNot("00000000000191", 1L)).thenReturn(true);

        assertTrue(gateway.existePorCnpjExcluindoId("00000000000191", 1L));
    }

    @Test
    void existePorCnpjExcluindoId_shouldReturnFalseWhenOnlyCurrentClienteHasCnpj() {
        Mockito.when(clienteRepository.existsByCnpjAndIdNot("00000000000191", 1L)).thenReturn(false);

        assertFalse(gateway.existePorCnpjExcluindoId("00000000000191", 1L));
    }

    @Test
    void existePorCnpjExcluindoId_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(clienteRepository.existsByCnpjAndIdNot(Mockito.anyString(), Mockito.anyLong()))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.existePorCnpjExcluindoId("00000000000191", 1L));
    }

    // -------------------------------------------------------------------------
    // listar
    // -------------------------------------------------------------------------

    @Test
    void listar_shouldReturnMappedPaginaWithCpf() {
        var entity = ClienteEntity.builder().id(1L).nome("Pedro").sobrenome("Silva").cpf("12345678909").build();
        var springPage = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        Mockito.when(clienteRepository.findAll(PageRequest.of(0, 10))).thenReturn(springPage);

        var resultado = gateway.listar(0, 10);

        assertEquals(1, resultado.content().size());
        assertEquals(1L, resultado.totalElements());
        assertEquals(1, resultado.totalPages());
        var cliente = resultado.content().getFirst();
        assertEquals(1L, cliente.getId());
        assertEquals("Pedro", cliente.getNomeCompleto().nome());
        assertEquals("12345678909", cliente.getCpf().get().getValor());
        assertTrue(cliente.getCnpj().isEmpty());
    }

    @Test
    void listar_shouldReturnMappedPaginaWithCnpj() {
        var entity = ClienteEntity.builder().id(2L).nome("Empresa").sobrenome("LTDA").cnpj("00000000000191").build();
        var springPage = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        Mockito.when(clienteRepository.findAll(PageRequest.of(0, 10))).thenReturn(springPage);

        var resultado = gateway.listar(0, 10);

        var cliente = resultado.content().getFirst();
        assertEquals("00000000000191", cliente.getCnpj().get().getValor());
        assertTrue(cliente.getCpf().isEmpty());
    }

    @Test
    void listar_shouldReturnEmptyPaginaWhenNoResults() {
        var springPage = new PageImpl<ClienteEntity>(List.of(), PageRequest.of(0, 10), 0);
        Mockito.when(clienteRepository.findAll(PageRequest.of(0, 10))).thenReturn(springPage);

        var resultado = gateway.listar(0, 10);

        assertTrue(resultado.content().isEmpty());
        assertEquals(0L, resultado.totalElements());
    }

    @Test
    void listar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(clienteRepository.findAll(Mockito.any(PageRequest.class)))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.listar(0, 10));
    }

    // -------------------------------------------------------------------------
    // existePorCpf
    // -------------------------------------------------------------------------

    @Test
    void existePorCpf_shouldReturnTrueWhenCpfExists() {
        Mockito.when(clienteRepository.existsByCpf("12345678909")).thenReturn(true);

        assertTrue(gateway.existePorCpf("12345678909"));
    }

    @Test
    void existePorCpf_shouldReturnFalseWhenCpfNotExists() {
        Mockito.when(clienteRepository.existsByCpf("12345678909")).thenReturn(false);

        assertFalse(gateway.existePorCpf("12345678909"));
    }

    @Test
    void existePorCpf_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(clienteRepository.existsByCpf(Mockito.anyString()))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.existePorCpf("12345678909"));
    }

    // -------------------------------------------------------------------------
    // existePorCnpj
    // -------------------------------------------------------------------------

    @Test
    void existePorCnpj_shouldReturnTrueWhenCnpjExists() {
        Mockito.when(clienteRepository.existsByCnpj("00000000000191")).thenReturn(true);

        assertTrue(gateway.existePorCnpj("00000000000191"));
    }

    @Test
    void existePorCnpj_shouldReturnFalseWhenCnpjNotExists() {
        Mockito.when(clienteRepository.existsByCnpj("00000000000191")).thenReturn(false);

        assertFalse(gateway.existePorCnpj("00000000000191"));
    }

    @Test
    void existePorCnpj_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(clienteRepository.existsByCnpj(Mockito.anyString()))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.existePorCnpj("00000000000191"));
    }
}
