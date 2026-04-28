package com.fiap.mecanica.ordemdeservico.infra.gateway.database;

import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico.StatusOrdemDeServico;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoInsumoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoPecaEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.OrdemDeServicoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.entity.ServicoEntity;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OrdemDeServicoInsumoRepository;
import com.fiap.mecanica.ordemdeservico.infra.gateway.repository.OrdemDeServicoPecaRepository;
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

    @Mock
    private OrdemDeServicoPecaRepository ordemDeServicoPecaRepository;

    @Mock
    private OrdemDeServicoInsumoRepository ordemDeServicoInsumoRepository;

    private static final String DESCRICAO = "Barulho ao frear";

    @Test
    void criar_shouldSaveEntityWithAllFields() {
        var os = new OrdemDeServico(1L, 2L, 3L, DESCRICAO);
        var captor = ArgumentCaptor.forClass(OrdemDeServicoEntity.class);

        gateway.criar(os);

        Mockito.verify(ordemDeServicoRepository).save(captor.capture());
        var entity = captor.getValue();
        assertEquals(1L, entity.getClienteId());
        assertEquals(2L, entity.getVeiculoId());
        assertEquals(3L, entity.getAtendenteId());
        assertEquals(DESCRICAO, entity.getDescricao());
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
                () -> gateway.criar(new OrdemDeServico(1L, 2L, 3L, DESCRICAO)));
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
                .descricao(DESCRICAO)
                .dataCriacao(dataCriacao)
                .dataInicioDiagnostico(dataInicio)
                .dataConclusaoDiagnostico(null)
                .servicos(List.of(servicoEntity))
                .build();
        Mockito.when(ordemDeServicoRepository.findOrdemDeServicoById(10L)).thenReturn(Optional.of(entity));
        Mockito.when(ordemDeServicoPecaRepository.findByOrdemServicoId(10L)).thenReturn(List.of());
        Mockito.when(ordemDeServicoInsumoRepository.findByOrdemServicoId(10L)).thenReturn(List.of());

        var result = gateway.buscarPorId(10L);

        assertTrue(result.isPresent());
        var os = result.get();
        assertEquals(10L, os.getId());
        assertEquals(1L, os.getClienteId());
        assertEquals(2L, os.getVeiculoId());
        assertEquals(3L, os.getAtendenteId());
        assertEquals(5L, os.getMecanicoId());
        assertEquals(StatusOrdemDeServico.EM_DIAGNOSTICO, os.getStatus());
        assertEquals(DESCRICAO, os.getDescricao());
        assertEquals(dataCriacao, os.getDataCriacao());
        assertEquals(dataInicio, os.getDataInicioDiagnostico());
        assertNull(os.getDataConclusaoDiagnostico());
        assertEquals(List.of(7L), os.getServicoIds());
    }

    @Test
    void buscarPorId_shouldReconstitutePecasEInsumosVinculados() {
        var pecaEntity = OrdemDeServicoPecaEntity.builder()
                .id(1L).ordemServicoId(10L).pecaId(20L).quantidade(3).build();
        var insumoEntity = OrdemDeServicoInsumoEntity.builder()
                .id(2L).ordemServicoId(10L).insumoId(30L).quantidade(5).build();
        var entity = OrdemDeServicoEntity.builder()
                .id(10L).clienteId(1L).veiculoId(2L).atendenteId(3L).mecanicoId(5L)
                .status(StatusOrdemDeServico.EM_DIAGNOSTICO).descricao(DESCRICAO)
                .dataCriacao(LocalDateTime.now()).dataInicioDiagnostico(LocalDateTime.now())
                .dataConclusaoDiagnostico(null).servicos(List.of()).build();
        Mockito.when(ordemDeServicoRepository.findOrdemDeServicoById(10L)).thenReturn(Optional.of(entity));
        Mockito.when(ordemDeServicoPecaRepository.findByOrdemServicoId(10L)).thenReturn(List.of(pecaEntity));
        Mockito.when(ordemDeServicoInsumoRepository.findByOrdemServicoId(10L)).thenReturn(List.of(insumoEntity));

        var result = gateway.buscarPorId(10L);

        assertTrue(result.isPresent());
        var os = result.get();
        assertEquals(1, os.getPecasVinculadas().size());
        assertEquals(20L, os.getPecasVinculadas().getFirst().pecaId());
        assertEquals(3, os.getPecasVinculadas().getFirst().quantidade());
        assertEquals(1, os.getInsumosVinculados().size());
        assertEquals(30L, os.getInsumosVinculados().getFirst().insumoId());
        assertEquals(5, os.getInsumosVinculados().getFirst().quantidade());
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
    void atualizar_shouldCallQueryWithMutableFieldsOnly() {
        var dataInicio = LocalDateTime.of(2026, 1, 10, 10, 0);
        var dataConclusao = LocalDateTime.of(2026, 1, 10, 11, 0);
        var os = OrdemDeServico.reconstituir(10L, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, DESCRICAO, LocalDateTime.now(), dataInicio, dataConclusao, List.of(), List.of(), List.of());

        gateway.atualizar(os);

        Mockito.verify(ordemDeServicoRepository).atualizar(
                10L, 5L, StatusOrdemDeServico.DIAGNOSTICO_CONCLUIDO, dataInicio, dataConclusao);
        Mockito.verify(ordemDeServicoRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void atualizar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.doThrow(new RuntimeException("db error"))
                .when(ordemDeServicoRepository).atualizar(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        var os = OrdemDeServico.reconstituir(10L, 1L, 2L, 3L, 5L,
                StatusOrdemDeServico.EM_DIAGNOSTICO, DESCRICAO, LocalDateTime.now(), LocalDateTime.now(), null, List.of(), List.of(), List.of());

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


    @Test
    void vincularOuSomarPeca_shouldSaveNewEntityWhenPecaNaoVinculada() {
        Mockito.when(ordemDeServicoPecaRepository.findByOrdemServicoIdAndPecaId(1L, 20L))
                .thenReturn(Optional.empty());
        var captor = ArgumentCaptor.forClass(OrdemDeServicoPecaEntity.class);

        gateway.vincularOuSomarPeca(1L, 20L, 3);

        Mockito.verify(ordemDeServicoPecaRepository).save(captor.capture());
        var entity = captor.getValue();
        assertEquals(1L, entity.getOrdemServicoId());
        assertEquals(20L, entity.getPecaId());
        assertEquals(3, entity.getQuantidade());
        Mockito.verify(ordemDeServicoPecaRepository, Mockito.never()).somarQuantidade(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void vincularOuSomarPeca_shouldSomarQuantidadeWhenPecaJaVinculada() {
        var existingEntity = OrdemDeServicoPecaEntity.builder()
                .id(1L).ordemServicoId(1L).pecaId(20L).quantidade(3).build();
        Mockito.when(ordemDeServicoPecaRepository.findByOrdemServicoIdAndPecaId(1L, 20L))
                .thenReturn(Optional.of(existingEntity));

        gateway.vincularOuSomarPeca(1L, 20L, 2);

        Mockito.verify(ordemDeServicoPecaRepository).somarQuantidade(1L, 20L, 2);
        Mockito.verify(ordemDeServicoPecaRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void vincularOuSomarPeca_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(ordemDeServicoPecaRepository.findByOrdemServicoIdAndPecaId(Mockito.any(), Mockito.any()))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.vincularOuSomarPeca(1L, 20L, 2));
    }

    @Test
    void vincularOuSomarInsumo_shouldSaveNewEntityWhenInsumoNaoVinculado() {
        Mockito.when(ordemDeServicoInsumoRepository.findByOrdemServicoIdAndInsumoId(1L, 30L))
                .thenReturn(Optional.empty());
        var captor = ArgumentCaptor.forClass(OrdemDeServicoInsumoEntity.class);

        gateway.vincularOuSomarInsumo(1L, 30L, 5);

        Mockito.verify(ordemDeServicoInsumoRepository).save(captor.capture());
        var entity = captor.getValue();
        assertEquals(1L, entity.getOrdemServicoId());
        assertEquals(30L, entity.getInsumoId());
        assertEquals(5, entity.getQuantidade());
        Mockito.verify(ordemDeServicoInsumoRepository, Mockito.never()).somarQuantidade(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void vincularOuSomarInsumo_shouldSomarQuantidadeWhenInsumoJaVinculado() {
        var existingEntity = OrdemDeServicoInsumoEntity.builder()
                .id(1L).ordemServicoId(1L).insumoId(30L).quantidade(5).build();
        Mockito.when(ordemDeServicoInsumoRepository.findByOrdemServicoIdAndInsumoId(1L, 30L))
                .thenReturn(Optional.of(existingEntity));

        gateway.vincularOuSomarInsumo(1L, 30L, 3);

        Mockito.verify(ordemDeServicoInsumoRepository).somarQuantidade(1L, 30L, 3);
        Mockito.verify(ordemDeServicoInsumoRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void vincularOuSomarInsumo_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        Mockito.when(ordemDeServicoInsumoRepository.findByOrdemServicoIdAndInsumoId(Mockito.any(), Mockito.any()))
                .thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.vincularOuSomarInsumo(1L, 30L, 3));
    }
}
