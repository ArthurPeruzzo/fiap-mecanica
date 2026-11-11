# ADR-0009 — Saga de pagamento orquestrada pela OS (lado do fiap-mecanica)

Estende o [ADR-0008](0008-mensageria-rabbitmq-outbox.md): o mesmo mecanismo (RabbitMQ + outbox) passa a servir a
todos os comandos da OS, e a OS passa a também **consumir** respostas.

## Decisão

- **Novo estado `AGUARDANDO_PAGAMENTO`** entre `AGUARDANDO_APROVACAO` e `EM_EXECUCAO`. Aprovar o orçamento grava o novo
  status e o comando `SOLICITAR_PAGAMENTO` (para a cobrança) na outbox, na mesma transação. A baixa do estoque deixa de
  ser uma chamada REST na aprovação: só acontece depois do pagamento ("cobrar primeiro, baixar depois"), então nenhuma
  falha exige estorno e a compensação é sempre liberar a reserva e cancelar a OS.
- **Outbox genérica:** cada linha carrega `exchange` e `routing_key`; o relay entrega ao serviço certo
  (`estoque.comandos`, `cobranca.comandos`).
- **Uma fila única de respostas** `ordemservico.respostas` (DLQ `ordemservico.respostas.dlq`), ligada ao exchange
  `ordemservico.eventos` por uma routing key por resposta; cobrança e estoque publicam nele. O listener decide o passo
  pela routing key.

| Resposta (routing key) | Origem | Efeito na OS |
|---|---|---|
| `pagamento.confirmado` | cobrança | grava `CONFIRMAR_BAIXA` (estoque) na outbox; OS segue `AGUARDANDO_PAGAMENTO` |
| `baixa.confirmada` | estoque | `AGUARDANDO_PAGAMENTO` → `EM_EXECUCAO` (fim da saga) |
| `pagamento.recusado` | cobrança | `AGUARDANDO_PAGAMENTO` → `CANCELADA` + `LIBERAR_RESERVA` na outbox + notifica o cliente |
| `reserva.liberada` | estoque | só registra o fim da compensação |

- **Idempotência:** resposta com a OS fora de `AGUARDANDO_PAGAMENTO` é confirmada sem efeito (repetida, atrasada ou
  após cancelamento). Mensagem sem `ordemServicoId` ou com routing key desconhecida vai direto para a DLQ; falha técnica
  passa pelo retry (1 s, 2 s, 4 s) e depois pela DLQ.
- **Permissões no broker:** o usuário `mecanica` é dono de `ordemservico.*` e publica em `estoque.comandos` e
  `cobranca.comandos`; `estoque` e `cobranca` publicam em `ordemservico.eventos` (ver `docker/rabbitmq/init.sh`).

## Consequências

- **Ordem na outbox:** o relay interrompe o lote na primeira linha que falha. Enquanto o estoque não declarar a fila de
  `reserva.confirmar-baixa` e a cobrança a de `pagamento.solicitar`, as linhas dessas rotas voltam como não roteadas e
  **bloqueiam as seguintes**. Os dois serviços precisam consumir esses comandos antes de a aprovação ir a um ambiente real.
- **Pagamento que nunca chega** deixa a OS em `AGUARDANDO_PAGAMENTO` e a reserva presa (risco aceito, sem expiração).
- **Sem cancelamento manual durante o pagamento.** `AGUARDANDO_PAGAMENTO` não aceita `cancelar`: o endpoint de recusa
  (`/orcamento/recusar/{id}`) responde 422 nesse status. Só a saga encerra a OS, por `pagamento.recusado`
  (`cancelarPorPagamentoRecusado`) ou pela resposta de baixa. Motivo (visto no teste ponta a ponta): cancelar à mão deixava a
  cobrança sem saber (pagamento `PENDENTE` podia virar `CONFIRMADO` de uma OS cancelada) e, se a baixa já estivesse em
  andamento, `CONFIRMAR_BAIXA` seguido de `LIBERAR_RESERVA` deixava o estoque baixado numa OS cancelada. Consequência: uma OS
  com pagamento pendente só sai dali quando o pagamento é decidido; cancelar com compensação (cancelar pagamento, devolver
  baixa) e estorno ficam como evolução.
- `dataAprovacao` continua sendo gravada na aprovação, então a duração de execução (`dataAprovacao` → `dataFinalizacao`)
  passa a incluir a espera do pagamento.
- `ReservaEstoqueGateway.confirmarReservas` (REST) não é mais usado pela OS; o endpoint do estoque segue existindo.

> **Atualização (11/11/2026):** os dois serviços já consomem os comandos `reserva.confirmar-baixa` e `pagamento.solicitar`, então o
> bloqueio da outbox descrito na primeira consequência deixou de valer em ambiente com os três serviços no ar. A saga foi validada
> ponta a ponta no API Gateway (fluxo feliz, pagamento recusado, orçamento recusado e as falhas de cobrança e de estoque; ver
> `scripts/validacao/`). O bloqueio continua possível se um dos serviços ainda não tiver subido: nesse caso as linhas esperam na
> outbox até a fila existir.
