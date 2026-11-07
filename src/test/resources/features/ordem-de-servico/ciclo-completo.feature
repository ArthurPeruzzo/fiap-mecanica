# language: pt
Funcionalidade: Ciclo de vida da ordem de serviço com controle de estoque
  Como atendente da oficina
  Quero acompanhar a ordem de serviço da abertura até a entrega do veículo
  Para que as peças e os insumos sejam reservados e baixados do estoque na hora certa

  Contexto:
    Dado que o estoque tem a peça 11 por 45 reais e o insumo 21 por 35 reais
    E que a oficina oferece o serviço de troca de óleo por 150 reais
    E que a oficina tem um atendente e um mecânico
    E que existe um cliente com um veículo cadastrado

  Cenário: OS aprovada é executada e entregue com a baixa do estoque
    Dado que o atendente abre uma OS com 2 unidades da peça 11 e 3 do insumo 21
    Então a OS está "RECEBIDA"
    E o estoque reservou 2 unidades da peça 11 e 3 do insumo 21 para a OS
    Quando o mecânico inicia e conclui o diagnóstico
    E o atendente envia o orçamento ao cliente
    Então a OS está "AGUARDANDO_APROVACAO"
    E o orçamento é de 345 reais
    E o estoque ainda não recebeu o pedido de baixa das reservas
    Quando o atendente aprova o orçamento
    Então a OS está "EM_EXECUCAO"
    E o estoque foi instruído a confirmar a baixa das reservas da OS
    Quando o mecânico executa e finaliza o serviço
    Então a OS está "FINALIZADA"
    Quando o atendente entrega o veículo
    Então a OS está "ENTREGUE"
    E o estoque não recebeu nenhum pedido de liberação das reservas
