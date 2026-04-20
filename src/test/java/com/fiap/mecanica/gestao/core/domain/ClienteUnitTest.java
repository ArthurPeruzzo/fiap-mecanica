package com.fiap.mecanica.gestao.core.domain;

import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteUnitTest {

    private static final NomeCompleto NOME = new NomeCompleto("Pedro", "Silva");

    @Test
    void shouldCreateClienteWithCpfOnly() {
        var cliente = new Cliente(NOME, null, "12345678909");

        assertTrue(cliente.getCpf().isPresent());
        assertEquals("12345678909", cliente.getCpf().get().getValor());
        assertTrue(cliente.getCnpj().isEmpty());
    }

    @Test
    void shouldStripCpfFormattingOnCreation() {
        var cliente = new Cliente(NOME, null, "123.456.789-09");

        assertEquals("12345678909", cliente.getCpf().get().getValor());
    }

    @Test
    void shouldCreateClienteWithCnpjOnly() {
        var cliente = new Cliente(NOME, "00000000000000", null);

        assertTrue(cliente.getCnpj().isPresent());
        assertEquals("00000000000000", cliente.getCnpj().get().getValor());
        assertTrue(cliente.getCpf().isEmpty());
    }

    @Test
    void shouldStripCnpjFormattingOnCreation() {
        var cliente = new Cliente(NOME, "00.000.000/0001-91", null);

        assertEquals("00000000000191", cliente.getCnpj().get().getValor());
    }

    @Test
    void shouldThrowWhenBothCpfAndCnpjProvided() {
        assertThrows(IllegalArgumentException.class,
                () -> new Cliente(NOME, "00000000000000", "12345678909"));
    }

    @Test
    void shouldThrowWhenNeitherCpfNorCnpjProvided() {
        assertThrows(IllegalArgumentException.class,
                () -> new Cliente(NOME, null, null));
    }

    @Test
    void shouldThrowWithCorrectMessageWhenNeitherDocumentoProvided() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> new Cliente(NOME, null, null));

        assertEquals("O cnpj ou cpf precisam estar preenchidos", ex.getMessage());
    }

    @Test
    void shouldStoreNomeCompleto() {
        var cliente = new Cliente(NOME, null, "12345678909");

        assertEquals("Pedro", cliente.getNomeCompleto().nome());
        assertEquals("Silva", cliente.getNomeCompleto().sobrenome());
    }

    @Test
    void reconstituir_shouldSetIdAndPreserveDocumento() {
        var cliente = Cliente.reconstituir(42L, NOME, null, "12345678909");

        assertEquals(42L, cliente.getId());
        assertTrue(cliente.getCpf().isPresent());
        assertEquals("12345678909", cliente.getCpf().get().getValor());
    }

    @Test
    void atualizar_shouldChangeNomeAndDocumento() {
        var cliente = new Cliente(NOME, null, "12345678909");

        cliente.atualizar(new NomeCompleto("Carlos", "Costa"), "00000000000191", null);

        assertEquals("Carlos", cliente.getNomeCompleto().nome());
        assertEquals("Costa", cliente.getNomeCompleto().sobrenome());
        assertTrue(cliente.getCnpj().isPresent());
        assertEquals("00000000000191", cliente.getCnpj().get().getValor());
        assertTrue(cliente.getCpf().isEmpty());
    }

    @Test
    void atualizar_shouldThrowWhenNeitherDocumentoProvided() {
        var cliente = new Cliente(NOME, null, "12345678909");

        assertThrows(IllegalArgumentException.class,
                () -> cliente.atualizar(NOME, null, null));
    }

    @Test
    void atualizar_shouldThrowWhenBothDocumentosProvided() {
        var cliente = new Cliente(NOME, null, "12345678909");

        assertThrows(IllegalArgumentException.class,
                () -> cliente.atualizar(NOME, "00000000000191", "12345678909"));
    }

    @Test
    void reconstituir_shouldWorkWithCnpj() {
        var cliente = Cliente.reconstituir(5L, NOME, "00000000000191", null);

        assertEquals(5L, cliente.getId());
        assertTrue(cliente.getCnpj().isPresent());
        assertEquals("00000000000191", cliente.getCnpj().get().getValor());
    }
}
