package com.fiap.mecanica.shared.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CnpjUnitTest {

    @Test
    void shouldStripFormattingAndStoreDigits() {
        var cnpj = new Cnpj("00.000.000/0000-00");

        assertEquals("00000000000000", cnpj.getValor());
    }

    @Test
    void shouldPreserveLettersInAlphanumericCnpj() {
        var cnpj = new Cnpj("1A.3BC.45D/0001-EF");

        assertEquals("1A3BC45D0001EF", cnpj.getValor());
    }

    @Test
    void shouldKeepRawValueUnchangedWhenNoFormatting() {
        var cnpj = new Cnpj("00000000000000");

        assertEquals("00000000000000", cnpj.getValor());
    }

    @ParameterizedTest
    @CsvSource({
            "00.000.000/0001-91, 00000000000191",
            "11.222.333/0001-81, 11222333000181",
            "AB.CDE.FGH/0001-IJ, ABCDEFGH0001IJ"
    })
    void shouldStripOnlyFormattingCharactersFromVariousFormats(String input, String expected) {
        assertEquals(expected, new Cnpj(input).getValor());
    }

    @Test
    void shouldImplementDocumentoInterface() {
        assertInstanceOf(Documento.class, new Cnpj("00000000000000"));
    }

    @Test
    void shouldFormatNumericCnpjWithDotsSlashAndDash() {
        var cnpj = new Cnpj("00000000000191");

        assertEquals("00.000.000/0001-91", cnpj.getValorFormatado());
    }

    @Test
    void shouldFormatAlphanumericCnpj() {
        var cnpj = new Cnpj("1A3BC45D0001EF");

        assertEquals("1A.3BC.45D/0001-EF", cnpj.getValorFormatado());
    }

    @Test
    void shouldFormatAfterStrippingInputFormatting() {
        var cnpj = new Cnpj("1A.3BC.45D/0001-EF");

        assertEquals("1A.3BC.45D/0001-EF", cnpj.getValorFormatado());
    }

    @ParameterizedTest
    @CsvSource({
            "00000000000191, 00.000.000/0001-91",
            "11222333000181, 11.222.333/0001-81",
            "ABCDEFGH0001IJ, AB.CDE.FGH/0001-IJ"
    })
    void shouldFormatVariousCnpjs(String raw, String expected) {
        assertEquals(expected, new Cnpj(raw).getValorFormatado());
    }
}
