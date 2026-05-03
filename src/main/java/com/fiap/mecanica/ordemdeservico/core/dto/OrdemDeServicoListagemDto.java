package com.fiap.mecanica.ordemdeservico.core.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class OrdemDeServicoListagemDto {
	private Long id;
	private String nomeCliente;
	private String documentoCliente;
	private String veiculo;
	private String nomeAtendente;
	private String nomeMecanico;
	private String status;
	private String descricao;
	private LocalDateTime dataCriacao;
	private LocalDateTime dataInicioDiagnostico;
	private LocalDateTime dataConclusaoDiagnostico;
	private List<ServicoVinculadoDto> servicos;
	private List<PecaVinculadaDto> pecas;
	private List<InsumoVinculadoDto> insumos;
	private BigDecimal valorTotal;
	private LocalDateTime dataEnvioOrcamento;
	private LocalDateTime dataCancelamento;
	private LocalDateTime dataAprovacao;
	private LocalDateTime dataFinalizacao;
	private LocalDateTime dataEntrega;
}
