package com.fiap.mecanica.ordemdeservico.core.domain.ordemdeservico;

import com.fiap.mecanica.ordemdeservico.core.exception.*;
import lombok.Getter;

import java.math.BigDecimal;
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
    private List<ServicoVinculado> servicosVinculados = new ArrayList<>();
    private List<PecaVinculada> pecasVinculadas = new ArrayList<>();
    private List<InsumoVinculado> insumosVinculados = new ArrayList<>();
    private Orcamento orcamento;

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

        if (servicosVinculados.isEmpty()) {
            throw new OrdemDeServicoSemServicosException();
        }
        state.concluirDiagnostico(this);
    }

    void transicionarPara(StatusOrdemDeServico novoStatus, OrdemDeServicoState novoState) {
        this.status = novoStatus;
        this.state = novoState;
    }

    public void vincularServico(Long servicoId, BigDecimal preco) {
        if (!StatusOrdemDeServico.EM_DIAGNOSTICO.equals(status)) {
            throw new VinculoServicoNaoAutorizadoException();
        }

        boolean jaVinculado = servicosVinculados.stream().anyMatch(s -> s.servicoId().equals(servicoId));
        if (jaVinculado) {
            throw new ServicoJaVinculadoException();
        }
        servicosVinculados.add(new ServicoVinculado(servicoId, preco));
    }

    public void desvincularServico(Long servicoId) {
        if (!StatusOrdemDeServico.EM_DIAGNOSTICO.equals(status)) {
            throw new VinculoServicoNaoAutorizadoException();
        }

        var existente = servicosVinculados.stream()
                .filter(s -> s.servicoId().equals(servicoId))
                .findFirst().orElseThrow(ServicoNaoVinculadoException::new);
        servicosVinculados.remove(existente);
    }

    public boolean possuiMecanicoResponsavel() {
        return mecanicoId != null;
    }

    public boolean isMecanicoResponsavel(Long mecanicoId) {
        return this.mecanicoId != null && this.mecanicoId.equals(mecanicoId);
    }

    public void vincularPeca(Long pecaId, Integer quantidade, BigDecimal preco) {
        if (!StatusOrdemDeServico.EM_DIAGNOSTICO.equals(status)) {
            throw new VinculoPecaNaoAutorizadaException();
        }
        var existente = pecasVinculadas.stream()
                .filter(p -> p.pecaId().equals(pecaId))
                .findFirst();
        if (existente.isPresent()) {
            pecasVinculadas.remove(existente.get());
            pecasVinculadas.add(new PecaVinculada(pecaId, existente.get().quantidade() + quantidade, preco));
        } else {
            pecasVinculadas.add(new PecaVinculada(pecaId, quantidade, preco));
        }
    }

    public void desvincularPeca(Long pecaId, Integer quantidade) {
        if (!StatusOrdemDeServico.EM_DIAGNOSTICO.equals(status)) {
            throw new DesvincularPecaNaoAutorizadaException();
        }

        var existente = pecasVinculadas.stream()
                .filter(p -> p.pecaId().equals(pecaId))
                .findFirst().orElseThrow(PecaNaoVinculadaException::new);

        if (quantidade > existente.quantidade()) {
            throw new QuantidadeDesvincularInvalidaException();
        }

        pecasVinculadas.remove(existente);
        int novaQuantidade = existente.quantidade() - quantidade;
        if (novaQuantidade > 0) {
            pecasVinculadas.add(new PecaVinculada(pecaId, novaQuantidade, existente.preco()));
        }
    }

    public void vincularInsumo(Long insumoId, Integer quantidade) {
        if (!StatusOrdemDeServico.EM_DIAGNOSTICO.equals(status)) {
            throw new VinculoInsumoNaoAutorizadaException();
        }
        var existente = insumosVinculados.stream()
                .filter(i -> i.insumoId().equals(insumoId))
                .findFirst();
        if (existente.isPresent()) {
            insumosVinculados.remove(existente.get());
            insumosVinculados.add(new InsumoVinculado(insumoId, existente.get().quantidade() + quantidade));
        } else {
            insumosVinculados.add(new InsumoVinculado(insumoId, quantidade));
        }
    }

    public void desvincularInsumo(Long insumoId, Integer quantidade) {
        if (!StatusOrdemDeServico.EM_DIAGNOSTICO.equals(status)) {
            throw new DesvincularInsumoNaoAutorizadaException();
        }

        var existente = insumosVinculados.stream()
                .filter(i -> i.insumoId().equals(insumoId))
                .findFirst().orElseThrow(InsumoNaoVinculadoException::new);

        if (quantidade > existente.quantidade()) {
            throw new QuantidadeDesvincularInvalidaException();
        }

        insumosVinculados.remove(existente);
        int novaQuantidade = existente.quantidade() - quantidade;
        if (novaQuantidade > 0) {
            insumosVinculados.add(new InsumoVinculado(insumoId, novaQuantidade));
        }
    }

    void calcularOrcamento() {
        var totalPecas = pecasVinculadas.stream()
                .map(p -> p.preco().multiply(BigDecimal.valueOf(p.quantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var totalServicos = servicosVinculados.stream()
                .map(ServicoVinculado::preco)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.orcamento = new Orcamento(totalPecas.add(totalServicos));
    }

    public static OrdemDeServico reconstituir(Long id, Long clienteId, Long veiculoId, Long atendenteId,
                                              Long mecanicoId, StatusOrdemDeServico status, String descricao,
                                              LocalDateTime dataCriacao, LocalDateTime dataInicioDiagnostico,
                                              LocalDateTime dataConclusaoDiagnostico,
                                              List<ServicoVinculado> servicosVinculados,
                                              List<PecaVinculada> pecasVinculadas,
                                              List<InsumoVinculado> insumosVinculados,
                                              Orcamento orcamento) {
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
        os.servicosVinculados = new ArrayList<>(servicosVinculados);
        os.pecasVinculadas = new ArrayList<>(pecasVinculadas);
        os.insumosVinculados = new ArrayList<>(insumosVinculados);
        os.orcamento = orcamento;
        return os;
    }
}
