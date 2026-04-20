package com.fiap.mecanica.shared.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CpfUnitTest {

    @Test
    void shouldStripFormattingAndStoreOnlyDigits() {
        var cpf = new Cpf("123.456.789-09");

        assertEquals("12345678909", cpf.getValor());
    }

    @Test
    void shouldKeepRawDigitsUnchanged() {
        var cpf = new Cpf("12345678909");

        assertEquals("12345678909", cpf.getValor());
    }

    @ParameterizedTest
    @CsvSource({
            "123.456.789-09, 12345678909",
            "000.000.000-00, 00000000000",
            "111.111.111-11, 11111111111"
    })
    void shouldStripDotsAndDashesFromVariousFormats(String input, String expected) {
        assertEquals(expected, new Cpf(input).getValor());
    }

    @Test
    void shouldImplementDocumentoInterface() {
        assertInstanceOf(Documento.class, new Cpf("12345678909"));
    }

    @Test
    void shouldFormatRawDigitsWithDotsAndDash() {
        var cpf = new Cpf("12345678909");

        assertEquals("123.456.789-09", cpf.getValorFormatado());
    }

    @Test
    void shouldFormatAfterStrippingInputFormatting() {
        var cpf = new Cpf("123.456.789-09");

        assertEquals("123.456.789-09", cpf.getValorFormatado());
    }

    @ParameterizedTest
    @CsvSource({
            "95114752073, 951.147.520-73",
            "00000000000, 000.000.000-00",
            "18825469039, 188.254.690-39"
    })
    void shouldFormatVariousCpfs(String raw, String expected) {
        assertEquals(expected, new Cpf(raw).getValorFormatado());
    }
}
