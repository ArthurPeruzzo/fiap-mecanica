# ADR-0002 — Comunicação síncrona por HTTP/REST, sem broker de mensagens

## Contexto

A solução tem três limites de integração:

1. Cliente/atendente → aplicação (API REST).
2. API Gateway → aplicação (roteamento) e API Gateway → Lambda de autenticação.
3. Lambda de autenticação → aplicação (`GET /authenticate/cliente/status`).

Há também a **notificação ao cliente** (orçamento enviado, OS finalizada, etc.), que é
conceitualmente assíncrona.

## Decisão

Toda a comunicação entre componentes é **síncrona, request/response sobre HTTP**:

- API Gateway usa integração `HTTP_PROXY` para a aplicação (`ANY /{proxy+}`) e `AWS_PROXY` para a
  Lambda (`POST /auth/cliente`).
- A Lambda chama a aplicação por HTTP direto no ELB.
- A comunicação entre os módulos Terraform é leitura de estado (`terraform_remote_state`), não um
  fluxo em runtime.
- **Não há broker de mensagens** (SNS/SQS/Kafka/RabbitMQ). A notificação ao cliente é modelada
  como uma porta de saída (`NotificacaoGateway`) e hoje implementada por um mock que registra em
  log (`NotificacaoMockGateway`). Trocar para e-mail/SNS depois é substituir a implementação,
  sem tocar no domínio.

## Consequências

**Positivas**
- Fluxo linear e fácil de depurar; um trace cobre a requisição inteira.
- Menos infraestrutura para provisionar e operar (sem fila, sem tópico, sem *dead-letter*).
- A cadeia de timeouts é explícita e controlável: `HTTP 15s < Lambda 25s < API Gateway 30s`.

**Negativas / trade-offs**
- Acoplamento temporal: se a aplicação está fora do ar, a autenticação de cliente falha na hora
  (`502`/`504` tratados pela Lambda).
- A notificação real, quando implementada, herdará o acoplamento síncrono a menos que se
  introduza uma fila — o que exigiria um novo ADR.
- Sem *buffering* nem *replay* de eventos.

## Alternativas consideradas

- **Broker (SNS/SQS)** para notificações e/ou eventos de mudança de status — mais resiliente,
  mas adiciona infraestrutura e complexidade de entrega/idempotência sem demanda real no escopo
  acadêmico. A `NotificacaoGateway` deixa essa porta aberta.
- **Lambda consultando o RDS diretamente** em vez de chamar a aplicação — descartado: duplicaria
  regra de negócio (existência/criação do `User` cliente) fora da aplicação. Ver
  [ADR-0006](0006-jwt-hs256-segredo-compartilhado.md).
