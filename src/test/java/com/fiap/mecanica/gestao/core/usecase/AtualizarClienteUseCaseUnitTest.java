package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.dto.AtualizarClienteDto;
import com.fiap.mecanica.gestao.core.exception.ClienteJaExisteException;
import com.fiap.mecanica.gestao.core.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AtualizarClienteUseCaseUnitTest {

	@InjectMocks
	private AtualizarClienteUseCase atualizarClienteUseCase;

	@Mock
	private ClienteGateway clienteGateway;

	@Test
	void shouldUpdateClienteWithCpfSuccessfully() {
		var clienteExistente = Cliente.reconstituir(1L, new NomeCompleto("Pedro", "Silva"), null, "95114752073");
		var dto = new AtualizarClienteDto(1L, "Pedro", "Santos", null, "951.147.520-73");

		Mockito.when(clienteGateway.buscarPorId(1L)).thenReturn(Optional.of(clienteExistente));
		Mockito.when(clienteGateway.existePorCpfExcluindoId("95114752073", 1L)).thenReturn(false);

		atualizarClienteUseCase.atualizar(dto);

		Mockito.verify(clienteGateway).atualizar(clienteExistente);
	}

	@Test
	void shouldUpdateClienteWithCnpjSuccessfully() {
		var clienteExistente = Cliente.reconstituir(1L, new NomeCompleto("Empresa", "LTDA"), "00000000000191", null);
		var dto = new AtualizarClienteDto(1L, "Empresa", "SA", "00.000.000/0001-91", null);

		Mockito.when(clienteGateway.buscarPorId(1L)).thenReturn(Optional.of(clienteExistente));
		Mockito.when(clienteGateway.existePorCnpjExcluindoId("00000000000191", 1L)).thenReturn(false);

		atualizarClienteUseCase.atualizar(dto);

		Mockito.verify(clienteGateway).atualizar(clienteExistente);
	}

	@Test
	void shouldThrowClienteNaoEncontradoExceptionWhenClienteNotFound() {
		var dto = new AtualizarClienteDto(99L, "Pedro", "Silva", null, "951.147.520-73");

		Mockito.when(clienteGateway.buscarPorId(99L)).thenReturn(Optional.empty());

		Assertions.assertThrows(ClienteNaoEncontradoException.class,
				() -> atualizarClienteUseCase.atualizar(dto));

		Mockito.verify(clienteGateway, Mockito.never()).atualizar(Mockito.any());
	}

	@Test
	void shouldThrowClienteJaExisteExceptionWhenCpfBelongsToAnotherCliente() {
		var clienteExistente = Cliente.reconstituir(1L, new NomeCompleto("Pedro", "Silva"), null, "95114752073");
		var dto = new AtualizarClienteDto(1L, "Pedro", "Silva", null, "951.147.520-73");

		Mockito.when(clienteGateway.buscarPorId(1L)).thenReturn(Optional.of(clienteExistente));
		Mockito.when(clienteGateway.existePorCpfExcluindoId("95114752073", 1L)).thenReturn(true);

		Assertions.assertThrows(ClienteJaExisteException.class,
				() -> atualizarClienteUseCase.atualizar(dto));

		Mockito.verify(clienteGateway, Mockito.never()).atualizar(Mockito.any());
	}

	@Test
	void shouldThrowClienteJaExisteExceptionWhenCnpjBelongsToAnotherCliente() {
		var clienteExistente = Cliente.reconstituir(1L, new NomeCompleto("Empresa", "LTDA"), "00000000000191", null);
		var dto = new AtualizarClienteDto(1L, "Empresa", "LTDA", "00.000.000/0001-91", null);

		Mockito.when(clienteGateway.buscarPorId(1L)).thenReturn(Optional.of(clienteExistente));
		Mockito.when(clienteGateway.existePorCnpjExcluindoId("00000000000191", 1L)).thenReturn(true);

		Assertions.assertThrows(ClienteJaExisteException.class,
				() -> atualizarClienteUseCase.atualizar(dto));

		Mockito.verify(clienteGateway, Mockito.never()).atualizar(Mockito.any());
	}

	@Test
	void shouldUpdateNomeCompletoOnDomainObject() {
		var clienteExistente = Cliente.reconstituir(1L, new NomeCompleto("Pedro", "Silva"), null, "95114752073");
		var dto = new AtualizarClienteDto(1L, "Carlos", "Costa", null, "951.147.520-73");

		Mockito.when(clienteGateway.buscarPorId(1L)).thenReturn(Optional.of(clienteExistente));
		Mockito.when(clienteGateway.existePorCpfExcluindoId("95114752073", 1L)).thenReturn(false);

		atualizarClienteUseCase.atualizar(dto);

		Assertions.assertEquals("Carlos", clienteExistente.getNomeCompleto().nome());
		Assertions.assertEquals("Costa", clienteExistente.getNomeCompleto().sobrenome());
	}
}
