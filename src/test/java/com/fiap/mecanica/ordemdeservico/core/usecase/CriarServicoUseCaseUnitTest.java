package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.ordemdeservico.core.domain.servico.Servico;
import com.fiap.mecanica.ordemdeservico.core.dto.CriarServicoDto;
import com.fiap.mecanica.ordemdeservico.core.gateway.ServicoGateway;
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
class CriarServicoUseCaseUnitTest {

    @InjectMocks
    private CriarServicoUseCase criarServicoUseCase;

    @Mock
    private ServicoGateway servicoGateway;

    @Test
    void shouldDelegateToGatewayWithCorrectFields() {
        var dto = new CriarServicoDto("Troca de óleo", "Troca com filtro incluso", new BigDecimal("150.00"));
        var captor = ArgumentCaptor.forClass(Servico.class);

        criarServicoUseCase.criar(dto);

        Mockito.verify(servicoGateway).criar(captor.capture());
        var servico = captor.getValue();
        assertEquals("Troca de óleo", servico.getNome());
        assertEquals("Troca com filtro incluso", servico.getDescricao());
        assertEquals(new BigDecimal("150.00"), servico.getPreco());
    }

    @Test
    void shouldPropagateExceptionFromGateway() {
        var dto = new CriarServicoDto("Troca de óleo", "Desc", new BigDecimal("150.00"));
        Mockito.doThrow(new RuntimeException("erro no banco")).when(servicoGateway).criar(Mockito.any());

        assertThrows(RuntimeException.class, () -> criarServicoUseCase.criar(dto));
    }
}
