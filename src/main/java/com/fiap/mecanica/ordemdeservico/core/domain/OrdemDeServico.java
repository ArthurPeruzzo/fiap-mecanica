package com.fiap.mecanica.ordemdeservico.core.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OrdemDeServico {
    private Long id;
    private Long clienteId;
    private Long veiculoId;
    private Long atendenteId;
    private StatusOrdemDeServico status;
    private LocalDateTime dataCriacao;

    public OrdemDeServico(Long clienteId, Long veiculoId, Long atendenteId) {
        this.clienteId = clienteId;
        this.veiculoId = veiculoId;
        this.atendenteId = atendenteId;
        this.status = StatusOrdemDeServico.RECEBIDA;
        this.dataCriacao = LocalDateTime.now();
    }

    public static OrdemDeServico reconstituir(Long id, Long clienteId, Long veiculoId, Long atendenteId,
                                              StatusOrdemDeServico status, LocalDateTime dataCriacao) {
        var os = new OrdemDeServico(clienteId, veiculoId, atendenteId);
        os.id = id;
        os.status = status;
        os.dataCriacao = dataCriacao;
        return os;
    }
}
