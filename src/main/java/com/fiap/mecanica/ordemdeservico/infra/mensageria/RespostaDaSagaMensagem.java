package com.fiap.mecanica.ordemdeservico.infra.mensageria;

/** Corpo das respostas da saga (cobrança e estoque): só identifica a ordem de serviço a que a resposta se refere. */
public record RespostaDaSagaMensagem(Long ordemServicoId) {
}
