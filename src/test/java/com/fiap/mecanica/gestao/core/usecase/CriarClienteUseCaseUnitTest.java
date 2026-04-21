package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.dto.CriarClienteDto;
import com.fiap.mecanica.gestao.core.exception.ClienteJaExisteException;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CriarClienteUseCaseUnitTest {

	@InjectMocks
	private CriarClienteUseCase criarClienteUseCase;

	@Mock
	private ClienteGateway clienteGateway;

	@Test
	void shouldDelegateToGatewayWhenCpfProvided() {
		var dto = new CriarClienteDto("Pedro", "Silva", "951.147.520-73", null);

		Mockito.when(clienteGateway.existePorCpf(Mockito.anyString())).thenReturn(false);

		criarClienteUseCase.criar(dto);

		Mockito.verify(clienteGateway).existePorCpf("95114752073");
		Mockito.verify(clienteGateway).criar(Mockito.any(Cliente.class));
		Mockito.verifyNoMoreInteractions(clienteGateway);
	}

	@Test
	void shouldDelegateToGatewayWhenCnpjProvided() {
		var dto = new CriarClienteDto("Pedro", "Silva", null, "00.000.000/0001-91");

		Mockito.when(clienteGateway.existePorCnpj(Mockito.anyString())).thenReturn(false);

		criarClienteUseCase.criar(dto);

		Mockito.verify(clienteGateway).existePorCnpj("00000000000191");
		Mockito.verify(clienteGateway).criar(Mockito.any(Cliente.class));
		Mockito.verifyNoMoreInteractions(clienteGateway);
	}

	@Test
	void shouldPassCorrectNomeToGatewayWhenCpfProvided() {
		var dto = new CriarClienteDto("Pedro", "Silva", "951.147.520-73", null);
		var captor = ArgumentCaptor.forClass(Cliente.class);

		Mockito.when(clienteGateway.existePorCpf(Mockito.anyString())).thenReturn(false);

		criarClienteUseCase.criar(dto);

		Mockito.verify(clienteGateway).criar(captor.capture());
		var clienteCapturado = captor.getValue();
		Assertions.assertEquals("Pedro", clienteCapturado.getNomeCompleto().nome());
		Assertions.assertEquals("Silva", clienteCapturado.getNomeCompleto().sobrenome());
	}

	@Test
	void shouldPassCorrectNomeToGatewayWhenCnpjProvided() {
		var dto = new CriarClienteDto("Pedro", "Silva", null, "00.000.000/0001-91");
		var captor = ArgumentCaptor.forClass(Cliente.class);

		Mockito.when(clienteGateway.existePorCnpj(Mockito.anyString())).thenReturn(false);

		criarClienteUseCase.criar(dto);

		Mockito.verify(clienteGateway).criar(captor.capture());
		var clienteCapturado = captor.getValue();
		Assertions.assertEquals("Pedro", clienteCapturado.getNomeCompleto().nome());
		Assertions.assertEquals("Silva", clienteCapturado.getNomeCompleto().sobrenome());
	}

	@Test
	void shouldThrowClienteJaExisteExceptionWhenCpfAlreadyExists() {
		var dto = new CriarClienteDto("Pedro", "Silva", "951.147.520-73", null);

		Mockito.when(clienteGateway.existePorCpf("95114752073")).thenReturn(true);

		Assertions.assertThrows(ClienteJaExisteException.class, () -> criarClienteUseCase.criar(dto));

		Mockito.verify(clienteGateway).existePorCpf("95114752073");
		Mockito.verify(clienteGateway, Mockito.never()).criar(Mockito.any());
	}

	@Test
	void shouldThrowClienteJaExisteExceptionWhenCnpjAlreadyExists() {
		var dto = new CriarClienteDto("Pedro", "Silva", null, "00.000.000/0001-91");

		Mockito.when(clienteGateway.existePorCnpj("00000000000191")).thenReturn(true);

		Assertions.assertThrows(ClienteJaExisteException.class, () -> criarClienteUseCase.criar(dto));

		Mockito.verify(clienteGateway).existePorCnpj("00000000000191");
		Mockito.verify(clienteGateway, Mockito.never()).criar(Mockito.any());
	}

	@Test
	void shouldPropagateExceptionFromGateway() {
		var dto = new CriarClienteDto("Pedro", "Silva", "951.147.520-73", null);

		Mockito.when(clienteGateway.existePorCpf(Mockito.anyString())).thenReturn(false);
		Mockito.doThrow(new RuntimeException("erro no banco"))
				.when(clienteGateway).criar(Mockito.any(Cliente.class));

		var ex = Assertions.assertThrows(RuntimeException.class,
				() -> criarClienteUseCase.criar(dto));

		Assertions.assertEquals("erro no banco", ex.getMessage());
		Mockito.verify(clienteGateway).criar(Mockito.any(Cliente.class));
	}
}
