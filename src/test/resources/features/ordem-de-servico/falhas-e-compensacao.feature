# language: pt
Funcionalidade: Falhas na integração com o estoque e compensação
  Como responsável pelo sistema da oficina
  Quero que uma falha do estoque nunca deixe a OS ou as reservas num estado inconsistente
  Para que a operação possa ser repetida e o estoque não fique com reservas órfãs

  Contexto:
    Dado que o estoque tem a peça 11 por 45 reais e o insumo 21 por 35 reais
    E que a oficina oferece o serviço de troca de óleo por 150 reais
    E que a oficina tem um atendente e um mecânico
    E que existe um cliente com um veículo cadastrado

  Cenário: Estoque sem saldo impede a abertura da OS
    Dado que o estoque não tem saldo suficiente para a reserva
    Quando o atendente tenta abrir uma OS com 2 unidades da peça 11 e 3 do insumo 21
    Então o pedido é recusado com o status 422 e a mensagem "Estoque insuficiente para realizar a operação"
    E nenhuma OS foi gravada

  Cenário: Erro interno do estoque impede a abertura da OS
    Dado que o estoque responde com erro interno às reservas
    Quando o atendente tenta abrir uma OS com 2 unidades da peça 11 e 3 do insumo 21
    Então o pedido é recusado com o status 503
    E nenhuma OS foi gravada

  Cenário: Resposta perdida do estoque libera a reserva que pode ter sido feita
    Dado que o estoque demora mais do que o limite de espera para responder à reserva
    Quando o atendente tenta abrir uma OS com 2 unidades da peça 11 e 3 do insumo 21
    Então o pedido é recusado com o status 503
    E nenhuma OS foi gravada
    E o estoque foi instruído a liberar as reservas que possam ter sido feitas

  Cenário: Vincular uma peça sem saldo deixa a OS intacta
    Dado que uma OS está em diagnóstico com o mecânico responsável
    E que o estoque não tem saldo suficiente para a reserva
    Quando o mecânico tenta vincular 5 unidades da peça 11 à OS
    Então o pedido é recusado com o status 422 e a mensagem "Estoque insuficiente para realizar a operação"
    E a OS não tem a peça 11 vinculada

  Cenário: Aprovação com o estoque fora do ar pode ser repetida sem baixar duas vezes
    Dado que o atendente abre uma OS com 2 unidades da peça 11 e 3 do insumo 21
    E o mecânico inicia e conclui o diagnóstico
    E o atendente envia o orçamento ao cliente
    E que o estoque está fora do ar
    Quando o atendente tenta aprovar o orçamento
    Então o pedido é recusado com o status 503
    E a OS está "AGUARDANDO_APROVACAO"
    Quando o estoque volta a responder
    E o atendente aprova o orçamento
    Então a OS está "EM_EXECUCAO"
    E a baixa das reservas foi confirmada uma única vez com sucesso

  Cenário: Cancelamento não depende do estoque e agenda a liberação das reservas
    Dado que o atendente abre uma OS com 2 unidades da peça 11 e 3 do insumo 21
    E o mecânico inicia e conclui o diagnóstico
    E o atendente envia o orçamento ao cliente
    E que o estoque está fora do ar
    Quando o atendente recusa o orçamento
    Então a OS está "CANCELADA"
    E o estoque não recebeu nenhum pedido de liberação das reservas
    E a liberação das reservas da OS ficou agendada para quando o estoque voltar
