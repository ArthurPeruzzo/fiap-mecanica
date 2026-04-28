package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.exception.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class OrdemDeServico {
    private Long id;
    private Long clienteId;
    private Long veiculoId;
    private Long atendenteId;
    private Long mecanicoId;
    private StatusOrdemDeServico status;
    private String descricao;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataInicioDiagnostico;
    private LocalDateTime dataConclusaoDiagnostico;
    private List<Long> servicoIds = new ArrayList<>();
    private List<PecaVinculada> pecasVinculadas = new ArrayList<>();

    private OrdemDeServicoState state;

    public OrdemDeServico(Long clienteId, Long veiculoId, Long atendenteId, String descricao) {
        this.clienteId = clienteId;
        this.veiculoId = veiculoId;
        this.atendenteId = atendenteId;
        this.descricao = descricao;
        this.status = StatusOrdemDeServico.RECEBIDA;
        this.dataCriacao = LocalDateTime.now();
        this.state = new OrdemDeServicoRecebidaState();
    }

    private OrdemDeServico() {
    }

    void setMecanicoId(Long mecanicoId) {
        this.mecanicoId = mecanicoId;
    }

    void setDataInicioDiagnostico(LocalDateTime dataInicioDiagnostico) {
        this.dataInicioDiagnostico = dataInicioDiagnostico;
    }

    void setDataConclusaoDiagnostico(LocalDateTime dataConclusaoDiagnostico) {
        this.dataConclusaoDiagnostico = dataConclusaoDiagnostico;
    }

    public void iniciarDiagnostico(Long mecanicoId) {
        if (possuiMecanicoResponsavel() && !isMecanicoResponsavel(mecanicoId)) {
            throw new OrdemDeServicoMecanicoResponsavelException();
        }
        state.iniciarDiagnostico(this, mecanicoId);
    }

    public void concluirDiagnostico(Long mecanicoId) {
        if (!isMecanicoResponsavel(mecanicoId)) {
            throw new MecanicoNaoResponsavelPelaOrdemDeServicoException();
        }

        if (servicoIds.isEmpty()) {
            throw new OrdemDeServicoSemServicosException();
        }
        state.concluirDiagnostico(this);
    }

    void transicionarPara(StatusOrdemDeServico novoStatus, OrdemDeServicoState novoState) {
        this.status = novoStatus;
        this.state = novoState;
    }

    public void vincularServico(Long servicoId) {
        if (!StatusOrdemDeServico.EM_DIAGNOSTICO.equals(status)) {
            throw new VinculoServicoNaoAutorizadoException();
        }

        if (servicoIds.contains(servicoId)) {
            throw new ServicoJaVinculadoException();
        }
        servicoIds.add(servicoId);
    }

    public void desvincularServico(Long servicoId) {
        if (!StatusOrdemDeServico.EM_DIAGNOSTICO.equals(status)) {
            throw new VinculoServicoNaoAutorizadoException();
        }

        if (!servicoIds.contains(servicoId)) {
            throw new ServicoNaoVinculadoException();
        }
        servicoIds.remove(servicoId);
    }

    public boolean possuiMecanicoResponsavel() {
        return mecanicoId != null;
    }

    public boolean isMecanicoResponsavel(Long mecanicoId) {
        return this.mecanicoId != null && this.mecanicoId.equals(mecanicoId);
    }

    public void vincularPeca(Long pecaId, Integer quantidade) {
        if (!StatusOrdemDeServico.EM_DIAGNOSTICO.equals(status)) {
            throw new VinculoPecaNaoAutorizadaException();
        }
        var existente = pecasVinculadas.stream()
                .filter(p -> p.pecaId().equals(pecaId))
                .findFirst();
        if (existente.isPresent()) {
            pecasVinculadas.remove(existente.get());
            pecasVinculadas.add(new PecaVinculada(pecaId, existente.get().quantidade() + quantidade));
        } else {
            pecasVinculadas.add(new PecaVinculada(pecaId, quantidade));
        }
    }

    public static OrdemDeServico reconstituir(Long id, Long clienteId, Long veiculoId, Long atendenteId,
                                              Long mecanicoId, StatusOrdemDeServico status, String descricao,
                                              LocalDateTime dataCriacao, LocalDateTime dataInicioDiagnostico,
                                              LocalDateTime dataConclusaoDiagnostico, List<Long> servicoIds,
                                              List<PecaVinculada> pecasVinculadas) {
        var os = new OrdemDeServico();
        os.id = id;
        os.clienteId = clienteId;
        os.veiculoId = veiculoId;
        os.atendenteId = atendenteId;
        os.mecanicoId = mecanicoId;
        os.status = status;
        os.descricao = descricao;
        os.dataCriacao = dataCriacao;
        os.dataInicioDiagnostico = dataInicioDiagnostico;
        os.dataConclusaoDiagnostico = dataConclusaoDiagnostico;
        os.state = OrdemDeServicoStateFactory.from(status);
        os.servicoIds = new ArrayList<>(servicoIds);
        os.pecasVinculadas = new ArrayList<>(pecasVinculadas);
        return os;
    }
}
