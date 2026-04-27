package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.exception.ServicoJaVinculadoException;
import com.fiap.mecanica.ordemdeservico.core.exception.ServicoNaoVinculadoException;
import com.fiap.mecanica.ordemdeservico.core.exception.VinculoServicoNaoAutorizadoException;
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
    private LocalDateTime dataCriacao;
    private LocalDateTime dataInicioDiagnostico;
    private LocalDateTime dataConclusaoDiagnostico;
    private List<Long> servicoIds = new ArrayList<>();

    private OrdemDeServicoState state;

    public OrdemDeServico(Long clienteId, Long veiculoId, Long atendenteId) {
        this.clienteId = clienteId;
        this.veiculoId = veiculoId;
        this.atendenteId = atendenteId;
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
        state.iniciarDiagnostico(this, mecanicoId);
    }

    public void concluirDiagnostico() {
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

    public static OrdemDeServico reconstituir(Long id, Long clienteId, Long veiculoId, Long atendenteId,
                                              Long mecanicoId, StatusOrdemDeServico status, LocalDateTime dataCriacao,
                                              LocalDateTime dataInicioDiagnostico, LocalDateTime dataConclusaoDiagnostico,
                                              List<Long> servicoIds) {
        var os = new OrdemDeServico();
        os.id = id;
        os.clienteId = clienteId;
        os.veiculoId = veiculoId;
        os.atendenteId = atendenteId;
        os.mecanicoId = mecanicoId;
        os.status = status;
        os.dataCriacao = dataCriacao;
        os.dataInicioDiagnostico = dataInicioDiagnostico;
        os.dataConclusaoDiagnostico = dataConclusaoDiagnostico;
        os.state = OrdemDeServicoStateFactory.from(status);
        os.servicoIds = new ArrayList<>(servicoIds);
        return os;
    }
}
