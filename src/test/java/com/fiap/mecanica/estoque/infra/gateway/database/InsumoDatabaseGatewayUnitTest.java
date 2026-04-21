package com.fiap.mecanica.estoque.infra.gateway.database;

import com.fiap.mecanica.estoque.core.domain.Insumo;
import com.fiap.mecanica.estoque.core.domain.UnidadeMedida;
import com.fiap.mecanica.estoque.infra.gateway.entity.InsumoEntity;
import com.fiap.mecanica.estoque.infra.gateway.repository.InsumoRepository;
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
class InsumoDatabaseGatewayUnitTest {

    @InjectMocks
    private InsumoDatabaseGateway gateway;

    @Mock
    private InsumoRepository insumoRepository;

    @Test
    void criar_shouldSaveEntityWithCorrectFields() {
        var insumo = new Insumo("Óleo de motor", "Óleo 5W30", new BigDecimal("45.90"), UnidadeMedida.LITRO, 10);
        var captor = ArgumentCaptor.forClass(InsumoEntity.class);

        gateway.criar(insumo);

        Mockito.verify(insumoRepository).save(captor.capture());
        var entity = captor.getValue();
        assertEquals("Óleo de motor", entity.getNome());
        assertEquals("Óleo 5W30", entity.getDescricao());
        assertEquals(new BigDecimal("45.90"), entity.getPreco());
        assertEquals(10, entity.getQuantidadeEstoque());
        assertEquals(UnidadeMedida.LITRO, entity.getUnidadeMedida());
    }

    @Test
    void criar_shouldThrowErroAcessoBaseDeDadosExceptionWhenRepositoryFails() {
        var insumo = new Insumo("Óleo de motor", "Óleo 5W30", new BigDecimal("45.90"), UnidadeMedida.LITRO, 10);
        Mockito.when(insumoRepository.save(Mockito.any())).thenThrow(new RuntimeException("db error"));

        assertThrows(ErroAcessoBaseDeDadosException.class, () -> gateway.criar(insumo));
    }
}
