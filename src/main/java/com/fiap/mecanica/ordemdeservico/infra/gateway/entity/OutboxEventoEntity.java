package com.fiap.mecanica.ordemdeservico.infra.gateway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_evento")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OutboxEventoEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tipo", nullable = false, length = 60)
    private String tipo;

    @Column(name = "agregado_id", nullable = false)
    private Long agregadoId;

    @Column(name = "payload", nullable = false, length = 2000)
    private String payload;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "enviado_em")
    private LocalDateTime enviadoEm;

    @Column(name = "tentativas", nullable = false)
    private int tentativas;

    @Column(name = "ultimo_erro", length = 500)
    private String ultimoErro;

    public void marcarComoEnviado() {
        this.enviadoEm = LocalDateTime.now();
        this.ultimoErro = null;
    }

    public void registrarFalha(String erro) {
        this.tentativas++;
        this.ultimoErro = erro == null ? null : erro.substring(0, Math.min(erro.length(), 500));
    }
}
