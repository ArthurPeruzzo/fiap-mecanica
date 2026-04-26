package com.fiap.mecanica.ordemdeservico.core.domain;

import com.fiap.mecanica.ordemdeservico.core.exception.TransicaoDeStatusInvalidaException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OrdemDeServico {
    private Long id;
    private Long clienteId;
    private Long veiculoId;
    private Long atendenteId;
    private Long mecanicoId;
    private StatusOrdemDeServico status;
    private LocalDateTime dataCriacao;

    public OrdemDeServico(Long clienteId, Long veiculoId, Long atendenteId) {
        this.clienteId = clienteId;
        this.veiculoId = veiculoId;
        this.atendenteId = atendenteId;
        this.status = StatusOrdemDeServico.RECEBIDA;
        this.dataCriacao = LocalDateTime.now();
    }

    public void iniciarDiagnostico(Long mecanicoId) {
        if (this.status != StatusOrdemDeServico.RECEBIDA) {
            throw new TransicaoDeStatusInvalidaException();
        }
        this.mecanicoId = mecanicoId;
        this.status = StatusOrdemDeServico.EM_DIAGNOSTICO;
    }

    public static OrdemDeServico reconstituir(Long id, Long clienteId, Long veiculoId, Long atendenteId,
                                              Long mecanicoId, StatusOrdemDeServico status, LocalDateTime dataCriacao) {
        var os = new OrdemDeServico(clienteId, veiculoId, atendenteId);
        os.id = id;
        os.mecanicoId = mecanicoId;
        os.status = status;
        os.dataCriacao = dataCriacao;
        return os;
    }
}
