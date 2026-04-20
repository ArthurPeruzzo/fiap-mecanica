package com.fiap.mecanica.gestao.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.shared.valueobjects.NomeCompleto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DeletarClienteUseCaseUnitTest {

	@InjectMocks
	private DeletarClienteUseCase deletarClienteUseCase;

	@Mock
	private ClienteGateway clienteGateway;

	@Test
	void shouldDeletarClienteSuccessfully() {
		var cliente = Cliente.reconstituir(1L, new NomeCompleto("Pedro", "Silva"), null, "12345678909");
		Mockito.when(clienteGateway.buscarPorId(1L)).thenReturn(Optional.of(cliente));

		deletarClienteUseCase.deletar(1L);

		Mockito.verify(clienteGateway).deletar(1L);
	}

	@Test
	void shouldThrowClienteNaoEncontradoExceptionWhenClienteNotFound() {
		Mockito.when(clienteGateway.buscarPorId(99L)).thenReturn(Optional.empty());

		assertThrows(ClienteNaoEncontradoException.class,
				() -> deletarClienteUseCase.deletar(99L));

		Mockito.verify(clienteGateway, Mockito.never()).deletar(Mockito.any());
	}

	@Test
	void shouldNotCallDeletarWhenClienteNotFound() {
		Mockito.when(clienteGateway.buscarPorId(Mockito.anyLong())).thenReturn(Optional.empty());

		assertThrows(ClienteNaoEncontradoException.class,
				() -> deletarClienteUseCase.deletar(1L));

		Mockito.verify(clienteGateway, Mockito.never()).deletar(Mockito.anyLong());
	}
}
