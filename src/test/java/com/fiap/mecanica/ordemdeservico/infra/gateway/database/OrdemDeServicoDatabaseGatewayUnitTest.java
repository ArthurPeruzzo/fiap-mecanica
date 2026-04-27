package com.fiap.mecanica.ordemdeservico.infra.gateway.database;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.ServicoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OrdemDeServicoRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
        assertNull(entity.getMecanicoId());
        assertEquals(StatusOrdemDeServico.RECEBIDA, entity.getStatus());
        assertNotNull(entity.getDataCriacao());
        assertNull(entity.getDataInicioDiagnostico());
        assertNull(entity.getDataConclusaoDiagnostico());
    }

    @Test
    void criar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(ordemDeServicoRepository.save(Mockito.any()))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class,
                () -> gateway.criar(new OrdemDeServico(1L, 2L, 3L)));
    }

    @Test
    void buscarPorId_shouldReconstituteDomainFromEntityWithServicos() {
        var dataCriacao = LocalDateTime.of(2026, 1, 10, 9, 0);
        var dataInicio = LocalDateTime.of(2026, 1, 10, 10, 0);
        var servicoEntity = ServicoEntity.builder().id(7L).nome("Alinhamento").descricao("desc").preco(BigDecimal.TEN).build();
        var entity = OrdemDeServicoEntity.builder()
                .id(10L)
                .clienteId(1L)
                .veiculoId(2L)
                .atendenteId(3L)
                .mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO)
                .dataCriacao(dataCriacao)
                .dataInicioDiagnostico(dataInicio)
                .dataConclusaoDiagnostico(null)
                .servicos(List.of(servicoEntity))
                .build();
        Mockito.when(ordemDeServicoRepository.findOrdemDeServicoById(10L)).thenReturn(Optional.of(entity));

        var result = gateway.buscarPorId(10L);

        assertTrue(result.isPresent());
        var os = result.get();
        assertEquals(10L, os.getId());
        assertEquals(1L, os.getClienteId());
        assertEquals(2L, os.getVeiculoId());
        assertEquals(3L, os.getAtendenteId());
        assertEquals(5L, os.getMecanicoId());
        assertEquals(StatusOrdemDeServico.EM_DIAGNOSTICO, os.getStatus());
        assertEquals(dataCriacao, os.getDataCriacao());
        assertEquals(dataInicio, os.getDataInicioDiagnostico());
        assertNull(os.getDataConclusaoDiagnostico());
        assertEquals(List.of(7L), os.getServicoIds());
    }

    @Test
    void buscarPorId_shouldReturnEmptyWhenNotFound() {
        Mockito.when(ordemDeServicoRepository.findOrdemDeServicoById(99L)).thenReturn(Optional.empty());

        assertTrue(gateway.buscarPorId(99L).isEmpty());
    }

    @Test
    void buscarPorId_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(ordemDeServicoRepository.findOrdemDeServicoById(Mockito.any()))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.buscarPorId(1L));
    }

    @Test
    void atualizar_shouldSaveEntityWithAllFields() {
        var dataCriacao = LocalDateTime.of(2026, 1, 10, 9, 0);
        var dataInicio = LocalDateTime.of(2026, 1, 10, 10, 0);
        var dataConclusao = LocalDateTime.of(2026, 1, 10, 11, 0);
        var os = OrdemDeServico.reconstituir(10L, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, dataCriacao, dataInicio, dataConclusao, List.of());
        var captor = ArgumentCaptor.forClass(OrdemDeServicoEntity.class);

        gateway.atualizar(os);

        Mockito.verify(ordemDeServicoRepository).save(captor.capture());
        var entity = captor.getValue();
        assertEquals(10L, entity.getId());
        assertEquals(1L, entity.getClienteId());
        assertEquals(2L, entity.getVeiculoId());
        assertEquals(3L, entity.getAtendenteId());
        assertEquals(5L, entity.getMecanicoId());
        assertEquals(StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, entity.getStatus());
        assertEquals(dataCriacao, entity.getDataCriacao());
        assertEquals(dataInicio, entity.getDataInicioDiagnostico());
        assertEquals(dataConclusao, entity.getDataConclusaoDiagnostico());
    }

    @Test
    void atualizar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(ordemDeServicoRepository.save(Mockito.any()))
                .thenThrow(new RuntimeException("db error"));
        var os = OrdemDeServico.reconstituir(10L, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, LocalDateTime.now(), LocalDateTime.now(), null, List.of());

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.atualizar(os));
    }

    @Test
    void existeOrdemAbertaParaVeiculo_shouldReturnTrueWhenExists() {
        Mockito.when(ordemDeServicoRepository.existsByVeiculoIdAndStatusNotIn(
                        2L, List.of(StatusOrdemDeServico.FINALIZADA, StatusOrdemDeServico.ENTREGUE)))
                .thenReturn(true);

        assertTrue(gateway.existeOrdemAbertaParaVeiculo(2L));
    }

    @Test
    void existeOrdemAbertaParaVeiculo_shouldReturnFalseWhenNotExists() {
        Mockito.when(ordemDeServicoRepository.existsByVeiculoIdAndStatusNotIn(
                        2L, List.of(StatusOrdemDeServico.FINALIZADA, StatusOrdemDeServico.ENTREGUE)))
                .thenReturn(false);

        assertFalse(gateway.existeOrdemAbertaParaVeiculo(2L));
    }

    @Test
    void existeOrdemAbertaParaVeiculo_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(ordemDeServicoRepository.existsByVeiculoIdAndStatusNotIn(Mockito.any(), Mockito.any()))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.existeOrdemAbertaParaVeiculo(2L));
    }

    @Test
    void vincularServico_shouldCallRepository() {
        gateway.vincularServico(1L, 10L);

        Mockito.verify(ordemDeServicoRepository).vincularServico(1L, 10L);
    }

    @Test
    void vincularServico_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.doThrow(new RuntimeException("db error"))
                .when(ordemDeServicoRepository).vincularServico(Mockito.any(), Mockito.any());

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.vincularServico(1L, 10L));
    }

    @Test
    void desvincularServico_shouldCallRepository() {
        gateway.desvincularServico(1L, 10L);

        Mockito.verify(ordemDeServicoRepository).desvincularServico(1L, 10L);
    }

    @Test
    void desvincularServico_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.doThrow(new RuntimeException("db error"))
                .when(ordemDeServicoRepository).desvincularServico(Mockito.any(), Mockito.any());

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.desvincularServico(1L, 10L));
    }
}
