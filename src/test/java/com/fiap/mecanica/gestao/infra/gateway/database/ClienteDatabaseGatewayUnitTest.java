package com.fiap.mecanica.gestao.infra.gateway.database;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.infra.gateway.entity.ClienteEntity;
import com.fiap.mecanica.gestao.infra.gateway.repository.ClienteRepository;
import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ClienteDatabaseGatewayUnitTest {

    @InjectMocks
    private ClienteDatabaseGateway gateway;

    @Mock
    private ClienteRepository clienteRepository;

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
}
