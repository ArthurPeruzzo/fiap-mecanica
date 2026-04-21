package com.fiap.mecanica.estoque.infra.gateway.database;

import com.fiap.mecanica.estoque.core.domain.Peca;
import com.fiap.mecanica.estoque.infra.gateway.entity.PecaEntity;
import com.fiap.mecanica.estoque.infra.gateway.repository.PecaRepository;
import com.fiap.mecanica.shared.exception.ErroAcessoBaseDeDadosException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PecaDatabaseGatewayUnitTest {

    @InjectMocks
    private PecaDatabaseGateway gateway;

    @Mock
    private PecaRepository pecaRepository;

    @Test
    void criar_shouldSaveEntityWithCorrectFields() {
        var peca = new Peca("Filtro de óleo", "Filtro 1.0", new BigDecimal("29.90"), 10);
        var captor = ArgumentCaptor.forClass(PecaEntity.class);

        gateway.criar(peca);

        Mockito.verify(pecaRepository).save(captor.capture());
        var entity = captor.getValue();
        assertEquals("Filtro de óleo", entity.getNome());
        assertEquals("Filtro 1.0", entity.getDescricao());
        assertEquals(new BigDecimal("29.90"), entity.getPreco());
        assertEquals(10, entity.getQuantidadeEstoque());
    }

    @Test
    void criar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        var peca = new Peca("Filtro de óleo", "Filtro 1.0", new BigDecimal("29.90"), 10);
        Mockito.when(pecaRepository.save(Mockito.any())).thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.criar(peca));
    }
}
