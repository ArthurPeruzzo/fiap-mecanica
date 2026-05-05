package com.fiap.mecanica.ordemdeservico.infra.controller.json;

import com.fiap.mecanica.ordemdeservico.core.dto.OrdemDeServicoListagemDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(name = "OrdemDeServicoResponseJson", description = "Detalhamento de uma ordem de serviço")
public class OrdemDeServicoResponseJson {

    @Schema(description = "ID da ordem de serviço", example = "1")
    private Long id;

    @Schema(description = "Nome completo do cliente proprietário do veículo", example = "Carlos Eduardo Ferreira")
    private String nomeCliente;

    @Schema(description = "Documento do cliente (CPF ou CNPJ formatado)", example = "659.976.270-04")
    private String documentoCliente;

    @Schema(description = "Descrição resumida do veículo (modelo, ano e placa)", example = "Honda Civic 2019 MIC-1294")
    private String veiculo;

    @Schema(description = "Nome do atendente que abriu a ordem de serviço", example = "João Silva")
    private String nomeAtendente;

    @Schema(description = "Nome do mecânico responsável pelo diagnóstico e execução", example = "Pedro Souza")
    private String nomeMecanico;

    @Schema(description = "Status atual da ordem de serviço", example = "EM_DIAGNOSTICO",
            allowableValues = {"RECEBIDA", "EM_DIAGNOSTICO", "DIAGNOSTICO_CONCLUIDO",
                    "AGUARDANDO_APROVACAO", "EM_EXECUCAO", "FINALIZADA", "ENTREGUE", "CANCELADA"})
    private String status;

    @Schema(description = "Descrição do problema relatado pelo cliente")
    private String descricao;

    @Schema(description = "Data e hora de abertura da ordem de serviço")
    private LocalDateTime dataCriacao;

    @Schema(description = "Data e hora de início do diagnóstico")
    private LocalDateTime dataInicioDiagnostico;

    @Schema(description = "Data e hora de conclusão do diagnóstico")
    private LocalDateTime dataConclusaoDiagnostico;

    @Schema(description = "Serviços vinculados à ordem de serviço")
    private List<ServicoVinculadoResponseJson> servicos;

    @Schema(description = "Peças vinculadas à ordem de serviço")
    private List<PecaVinculadaResponseJson> pecas;

    @Schema(description = "Insumos vinculados à ordem de serviço")
    private List<InsumoVinculadoResponseJson> insumos;

    @Schema(description = "Valor total do orçamento (soma de serviços, peças e insumos)", example = "394.00")
    private BigDecimal valorTotal;

    @Schema(description = "Tempo médio de execução dos serviços finalizados. Null quando nenhum serviço foi finalizado.")
    private TempoMedioExecucaoResponseJson tempoMedioExecucaoServicos;

    @Schema(description = "Data e hora de envio do orçamento ao cliente")
    private LocalDateTime dataEnvioOrcamento;

    @Schema(description = "Data e hora de cancelamento da ordem")
    private LocalDateTime dataCancelamento;

    @Schema(description = "Data e hora de aprovação do orçamento pelo cliente")
    private LocalDateTime dataAprovacao;

    @Schema(description = "Data e hora de finalização de todos os serviços")
    private LocalDateTime dataFinalizacao;

    @Schema(description = "Data e hora de entrega do veículo ao cliente")
    private LocalDateTime dataEntrega;

    public static OrdemDeServicoResponseJson from(OrdemDeServicoListagemDto os) {
        return OrdemDeServicoResponseJson.builder()
                .id(os.getId())
                .nomeCliente(os.getNomeCliente())
                .documentoCliente(os.getDocumentoCliente())
                .veiculo(os.getVeiculo())
                .nomeAtendente(os.getNomeAtendente())
                .nomeMecanico(os.getNomeMecanico())
                .status(os.getStatus())
                .descricao(os.getDescricao())
                .dataCriacao(os.getDataCriacao())
                .dataInicioDiagnostico(os.getDataInicioDiagnostico())
                .dataConclusaoDiagnostico(os.getDataConclusaoDiagnostico())
                .servicos(os.getServicos().stream().map(ServicoVinculadoResponseJson::from).toList())
                .pecas(os.getPecas().stream().map(PecaVinculadaResponseJson::from).toList())
                .insumos(os.getInsumos().stream().map(InsumoVinculadoResponseJson::from).toList())
                .valorTotal(os.getValorTotal())
                .tempoMedioExecucaoServicos(TempoMedioExecucaoResponseJson.from(os.getTempoMedioExecucaoServicos()))
                .dataEnvioOrcamento(os.getDataEnvioOrcamento())
                .dataCancelamento(os.getDataCancelamento())
                .dataAprovacao(os.getDataAprovacao())
                .dataFinalizacao(os.getDataFinalizacao())
                .dataEntrega(os.getDataEntrega())
                .build();
    }
}
