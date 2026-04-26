package com.fiap.mecanica.ordemdeservico.core.usecase;

import com.fiap.mecanica.gestao.core.domain.Atendente;
import com.fiap.mecanica.gestao.core.domain.Cliente;
import com.fiap.mecanica.gestao.core.domain.Veiculo;
import com.fiap.mecanica.gestao.core.exception.ClienteNaoEncontradoException;
import com.fiap.mecanica.gestao.core.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.gestao.core.gateway.AtendenteGateway;
import com.fiap.mecanica.gestao.core.gateway.ClienteGateway;
import com.fiap.mecanica.gestao.core.gateway.VeiculoGateway;
import com.fiap.mecanica.ordemdeservico.core.domain.OrdemDeServico;
import com.fiap.mecanica.ordemdeservico.core.dto.CriarOrdemDeServicoDto;
import com.fiap.mecanica.gestao.core.exception.AtendenteNaoEncontradoException;
import com.fiap.mecanica.ordemdeservico.core.exception.OrdemDeServicoAbertaParaVeiculoException;
import com.fiap.mecanica.ordemdeservico.core.exception.VeiculoNaoPertenceAoClienteException;
import com.fiap.mecanica.ordemdeservico.core.gateway.OrdemDeServicoGateway;
import com.fiap.mecanica.shared.seguranca.core.gateway.TokenGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CriarOrdemDeServicoUseCase {

	private final AtendenteGateway atendenteGateway;
	private final TokenGateway tokenGateway;
	private final OrdemDeServicoGateway ordemDeServicoGateway;
	private final VeiculoGateway veiculoGateway;
	private final ClienteGateway clienteGateway;

	public void criar(CriarOrdemDeServicoDto dto) {
		Atendente atendente = buscaAtendentePorUsuarioId();
		Veiculo veiculo = buscaVeiculoPorId(dto.veiculoId());
		Cliente cliente = buscaClientePorId(dto.clienteId());

		if (!veiculo.pertenceAo(cliente.getId())) {
			throw new VeiculoNaoPertenceAoClienteException();
		}

		if (ordemDeServicoGateway.existeOrdemAbertaParaVeiculo(dto.veiculoId())) {
			throw new OrdemDeServicoAbertaParaVeiculoException();
		}

		OrdemDeServico ordemDeServico = new OrdemDeServico(dto.clienteId(), dto.veiculoId(), atendente.getId());
		ordemDeServicoGateway.criar(ordemDeServico);
	}

	private Atendente buscaAtendentePorUsuarioId() {
		Long userId = tokenGateway.getUserId();
		return atendenteGateway.findByUsuarioId(userId).orElseThrow(AtendenteNaoEncontradoException::new);
	}

	private Veiculo buscaVeiculoPorId(Long veiculoId) {
		return veiculoGateway.buscarPorId(veiculoId).orElseThrow(VeiculoNaoEncontradoException::new);
	}

	private Cliente buscaClientePorId(Long clienteId) {
		return clienteGateway.buscarPorId(clienteId).orElseThrow(ClienteNaoEncontradoException::new);
	}
}
