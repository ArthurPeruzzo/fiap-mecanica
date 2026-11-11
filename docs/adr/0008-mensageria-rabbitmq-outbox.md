# ADR-0008 — Comandos assíncronos ao estoque via RabbitMQ com outbox transacional

Substitui parcialmente o [ADR-0002](0002-comunicacao-sincrona-http.md) (que descartava o broker).

## Contexto

Com o estoque extraído para o `fiap-estoque`, recusar um orçamento passou a exigir uma chamada HTTP
síncrona (`POST /reserva/ordem-servico/{id}/liberar`) **antes** de gravar o cancelamento da OS. Isso
acoplava a disponibilidade do cancelamento à do estoque: com o estoque fora do ar a recusa devolvia
503 e a OS não era cancelada, embora o cancelamento seja uma decisão do cliente que não depende do
estoque para ser válida.

Trocar a chamada HTTP por "gravar a OS e depois publicar no broker" repetiria o problema da escrita
dupla já vivido no vincular/desvincular: são dois sistemas, não há transação que cubra os dois, e uma
queda entre as duas operações deixaria a OS `CANCELADA` com a reserva `ATIVA` para sempre.

## Decisão

Liberar as reservas de uma OS cancelada passa a ser um **comando assíncrono** entregue por RabbitMQ,
com **outbox transacional** no lado do monólito. O restante da integração com o estoque (reservar,
confirmar, catálogo) continua síncrono por HTTP, com o token de serviço do
[ADR-0007](0007-token-de-servico-entre-microsservicos.md).

**Fluxo**
1. `OrcamentoRecusadoOrdemDeServicoUseCase` grava a OS (`CANCELADA`) e o comando na tabela
   `outbox_evento` **na mesma transação** (`TransacaoGateway`). A notificação ao cliente ocorre depois.
2. O `OutboxRelay` (`@Scheduled`, intervalo de 1 s) lê as linhas pendentes
   (`SELECT ... FOR UPDATE SKIP LOCKED`, lote de 50) e publica no exchange `estoque.comandos`
   (direct, chave `reserva.liberar`), com mensagem persistente e `messageId` igual ao id da linha.
   O contexto de trace da requisição (`traceparent`) é gravado na mesma linha (`V19`) e o relay publica dentro de um
   span filho dele, com o `traceparent` no cabeçalho da mensagem: o consumo no estoque continua o **mesmo trace** do
   cancelamento, apesar de a publicação acontecer depois, em outra thread.
3. A linha só é marcada como enviada depois do *publisher confirm* do broker **e** sem mensagem
   devolvida como não roteada (`mandatory` + *returns*). Qualquer falha mantém a linha pendente,
   incrementa `tentativas`, grava `ultimo_erro` e interrompe o lote (preserva a ordem).
4. O `fiap-estoque` declara a fila `estoque.liberar-reserva` (com *dead-letter exchange*
   `estoque.comandos.dlx` e a fila `estoque.liberar-reserva.dlq`) e consome com o mesmo caso de uso
   do endpoint HTTP. Falha ao processar: retry com espera crescente (1 s, 2 s, 4 s; 3 retries depois
   da primeira execução). Esgotadas as tentativas, ou mensagem malformada (sem retry), vai para a DLQ.

**Controle de acesso no broker** (script `docker/rabbitmq/init.sh`): vhost `fiap`; o usuário do
monólito só configura/escreve no exchange `estoque.comandos` e não lê nada; o usuário do estoque é
dono dos recursos `estoque.*`. Credenciais por serviço, não compartilhadas.

**Operação**
- Os dois serviços exigem `RABBITMQ_USERNAME` e `RABBITMQ_PASSWORD` para subir (falha rápida).
- `management.health.rabbit.enabled=false` nos dois: broker fora do ar não derruba as probes.
- Broker de desenvolvimento no `docker-compose` do monólito (portas 5672 e 15672); o estoque, rodando
  no IDE ou em seu compose, conecta nele.

## Consequências

**Positivas**
- O cancelamento não depende mais do estoque nem do broker estarem no ar; a pendência fica na outbox
  e é entregue quando voltarem.
- Nenhuma mensagem se perde entre a gravação da OS e o broker: a intenção é durável no mesmo commit.
- Seguro com várias réplicas do monólito (HPA de 1 a 4): `SKIP LOCKED` distribui as linhas.
- Base reutilizável para os próximos comandos e para a Saga orquestrada.

**Negativas / trade-offs**
- **Consistência eventual:** `204` na recusa significa "cancelada", e não "estoque liberado". Entre
  os dois fatos a disponibilidade aparece **menor** que a real (nunca maior), então o erro vai na
  direção segura: no pior caso uma outra OS recebe 422 por uma reserva que logo seria liberada.
  Por isso a **aprovação continua síncrona**: lá o erro iria na direção perigosa (executar sem baixar).
- **Entrega "pelo menos uma vez":** o consumidor recebe duplicatas. É inofensivo porque liberar só age
  em reservas `ATIVA`; novos comandos precisam ser idempotentes pelo mesmo motivo.
- **Sem alarme pronto:** linha pendente antiga na outbox e mensagem na DLQ são os sinais de problema,
  mas ainda não há métrica nem alerta nem reprocessamento da DLQ (hoje é manual, pelo painel).
- **Latência do polling:** até ~1 s entre o commit e a publicação, mais uma consulta periódica
  (índice em `enviado_em`, `criado_em`).
- **Ordem entre linhas:** o relay interrompe o lote na primeira falha para manter a ordem, então uma
  linha que sempre falha (por exemplo, o estoque ainda não declarou a fila) bloqueia as seguintes até
  ser resolvida. É o comportamento desejado para um broker indisponível, e pesado para uma linha
  "venenosa"; hoje só há um tipo de comando e ele não tem esse modo de falha.
- **Fila declarada pelo consumidor:** se o monólito publicar antes de o estoque ter subido pela
  primeira vez, a mensagem volta como não roteada e fica pendente até a fila existir.
- **Propriedade de retry do Spring Boot 4:** o nome é `max-retries` (tentativas depois da primeira),
  e não `max-attempts`; o nome antigo é ignorado sem aviso. Coberto por teste.
- **Dependência do broker no cluster:** o RabbitMQ roda no namespace `mensageria`, publicado pelo CD do
  repositório `fiap-estoque` (StatefulSet com disco EBS; o Amazon MQ está bloqueado na conta Academy). O
  `cd.yml` deste repositório injeta `RABBITMQ_*` e `ESTOQUE_URL`; sem o broker no ar o monólito sobe e
  acumula os comandos na outbox, mas sem as variáveis ele falha na partida. O disco EBS (driver CSI com a
  `LabRole`) foi validado em conta real: no deploy de 10/11/2026 o StatefulSet subiu com o volume EBS `Bound`.

## Alternativas consideradas

- **Manter o HTTP síncrono** — simples, mas acopla o cancelamento à disponibilidade do estoque.
- **Publicar direto no broker após gravar a OS** — reintroduz a escrita dupla (queda entre as duas
  operações perde o comando).
- **Outbox com `@TransactionalEventListener(AFTER_COMMIT)`** — latência quase nula, mas sozinho não
  fecha a janela entre o commit e a publicação. Pode complementar o polling no futuro, se houver
  necessidade medida de latência.
- **CDC (Debezium lendo o log do MySQL)** — elimina a varredura, mas acrescenta um componente
  pesado ao projeto.
- **Coreografia (o estoque reage a um evento `OrdemCancelada`)** — o estoque passa a conhecer o ciclo
  de vida da OS. Preferiu-se a orquestração: o monólito, dono da OS, manda **comandos**.
- **Inbox/deduplicação por `messageId` no consumidor** — desnecessária enquanto o comando for
  naturalmente idempotente.

## Evolução prevista

1. ~~Resposta do estoque (`ReservaLiberada`) e estado da Saga no monólito~~ — feito no [ADR-0009](0009-saga-de-pagamento-orquestrada.md).
2. ~~Compensações reais (Saga orquestrada) no fluxo de aprovação~~ — feito no ADR-0009 (pagamento recusado cancela a OS e libera a reserva).
3. Métrica e alerta sobre a idade da linha mais antiga na outbox e sobre a DLQ; procedimento de reprocessamento. **Pendente.**
4. ~~Broker no cluster, credenciais no Secret e atualização do `cd.yml`~~ — feito: RabbitMQ no namespace `mensageria`, publicado pelo CD do `fiap-estoque`.
