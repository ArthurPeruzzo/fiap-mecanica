package com.fiap.mecanica.gestao.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlacaUnitTest {

    @ParameterizedTest
    @CsvSource({
            "ABC1234, ABC1234",
            "ABC-1234, ABC1234",
            "abc1234, ABC1234",
            "abc-1234, ABC1234",
            "ABC1D23, ABC1D23",
            "ABC-1D23, ABC1D23"
    })
    void shouldStripHyphenAndUppercase(String entrada, String esperado) {
        assertEquals(esperado, new Placa(entrada).getValor());
    }

    @ParameterizedTest
    @CsvSource({
            "ABC1234, ABC-1234",
            "ABC1D23, ABC-1D23"
    })
    void getValorFormatado_shouldInsertHyphenAfterThirdChar(String valor, String esperado) {
        assertEquals(esperado, new Placa(valor).getValorFormatado());
    }

    @Test
    void getValorFormatado_shouldHandleAlreadyFormattedInput() {
        assertEquals("ABC-1234", new Placa("ABC-1234").getValorFormatado());
    }
}
